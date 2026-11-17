use crate::json::json_value::{JsonObject, JsonValue};
use crate::json::parse_exception::ParseException;
use std::fmt::Display;

pub struct JsonPureStringParser<'i> {
    input: &'i str,
    index: isize,
    line: usize,
    column: usize,
    current: char,
    capture_buffer: String,
    capture_start: isize,
}

pub type JsonParseResult<S> = Result<S, ParseException>;

impl<'i> JsonPureStringParser<'i> {
    pub fn new(input: &'i str) -> Self {
        JsonPureStringParser {
            input,
            index: -1,
            line: 1,
            column: 1,
            current: 0 as char,
            capture_buffer: String::new(),
            capture_start: -1,
        }
    }

    pub fn parse(&mut self) -> JsonParseResult<JsonValue> {
        self.read();
        self.skip_whitespace();
        let result = self.read_value()?;
        self.skip_whitespace();
        if !self.is_end_of_text() {
            self.error("Unexpected character")
        } else {
            Ok(result)
        }
    }

    fn read_value(&mut self) -> JsonParseResult<JsonValue> {
        match self.current {
            'n' => self.read_null(),
            't' => self.read_true(),
            'f' => self.read_false(),
            '"' => self.read_string(),
            '[' => Ok(JsonValue::Array(self.read_array()?)),
            '{' => Ok(JsonValue::Object(Box::new(self.read_object()?))),
            '-' | '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9' => self.read_number(),
            _ => self.error("value"),
        }
    }

    fn read_array(&mut self) -> JsonParseResult<Vec<JsonValue>> {
        self.read();
        let mut array = Vec::new();
        self.skip_whitespace();
        if self.read_char(']') {
            return Ok(array);
        }
        loop {
            self.skip_whitespace();
            array.push(self.read_value()?);
            self.skip_whitespace();
            if !self.read_char(',') {
                break;
            }
        }
        if !self.read_char(']') {
            self.expected("',' or ']'")
        } else {
            Ok(array)
        }
    }

    fn read_object(&mut self) -> JsonParseResult<JsonObject> {
        self.read();
        let mut object = JsonObject::default();
        self.skip_whitespace();
        if self.read_char('}') {
            return Ok(object);
        }
        loop {
            self.skip_whitespace();
            let name = self.read_name()?;
            self.skip_whitespace();
            if !self.read_char(':') {
                return self.expected("':'");
            }
            self.skip_whitespace();
            object.add(name, self.read_value()?);
            self.skip_whitespace();
            if !self.read_char(',') {
                break;
            }
        }
        if !self.read_char('}') {
            self.expected("',' or '}'")
        } else {
            Ok(object)
        }
    }

    fn read_name(&mut self) -> JsonParseResult<String> {
        if self.current != '"' {
            return self.expected("name");
        }
        self.read_string_internal()
    }

    fn read_null(&mut self) -> JsonParseResult<JsonValue> {
        self.read();
        self.read_required_char('u')?;
        self.read_required_char('l')?;
        self.read_required_char('l')?;
        Ok(JsonValue::NULL)
    }

    fn read_true(&mut self) -> JsonParseResult<JsonValue> {
        self.read();
        self.read_required_char('r')?;
        self.read_required_char('u')?;
        self.read_required_char('e')?;
        Ok(JsonValue::TRUE)
    }

    fn read_false(&mut self) -> JsonParseResult<JsonValue> {
        self.read();
        self.read_required_char('a')?;
        self.read_required_char('l')?;
        self.read_required_char('s')?;
        self.read_required_char('e')?;
        Ok(JsonValue::FALSE)
    }

    fn read_required_char(&mut self, c: char) -> JsonParseResult<()> {
        if !self.read_char(c) {
            return self.expected(format!("'{}'", c));
        }
        Ok(())
    }

    fn read_string(&mut self) -> JsonParseResult<JsonValue> {
        Ok(JsonValue::String(self.read_string_internal()?))
    }

    fn read_string_internal(&mut self) -> JsonParseResult<String> {
        self.read();
        self.start_capture();
        while self.current != '"' {
            if self.current == '\\' {
                self.pause_capture();
                self.read_escape()?;
                self.start_capture();
            } else {
                self.read();
            }
        }
        let string = self.end_capture();
        self.read();
        Ok(string)
    }

    fn read_escape(&mut self) -> JsonParseResult<()> {
        self.read();
        match self.current {
            '"' | '/' | '\\' => self.capture_buffer.push(self.current),
            'b' => self.capture_buffer.push('\x08'),
            'f' => self.capture_buffer.push('\x0c'),
            'n' => self.capture_buffer.push('\n'),
            'r' => self.capture_buffer.push('\r'),
            't' => self.capture_buffer.push('\t'),
            _ => return self.expected("valid escape sequence"),
        }
        Ok(())
    }

    fn read_number(&mut self) -> JsonParseResult<JsonValue> {
        self.start_capture();
        self.read_char('-');
        let first_digit = self.current;
        if !self.read_digit() {
            return self.expected("digit");
        }
        if first_digit != '0' {
            while self.read_digit() {}
        }
        self.read_fraction()?;
        self.read_exponent()?;
        Ok(JsonValue::Number(self.end_capture()))
    }

    fn read_fraction(&mut self) -> JsonParseResult<bool> {
        if !self.read_char('.') {
            return Ok(false);
        }
        if !self.read_digit() {
            return self.expected("digit");
        }
        while self.read_digit() {}
        Ok(true)
    }

    fn read_exponent(&mut self) -> JsonParseResult<bool> {
        if !self.read_char('e') && !self.read_char('E') {
            return Ok(false);
        }
        if !self.read_char('+') {
            self.read_char('-');
        }
        if !self.read_digit() {
            return self.expected("digit");
        }
        while self.read_digit() {}
        Ok(true)
    }

    fn read_char(&mut self, ch: char) -> bool {
        if self.current != ch {
            return false;
        }
        self.read();
        true
    }

    fn read_digit(&mut self) -> bool {
        if !self.is_digit() {
            return false;
        }
        self.read();
        true
    }

    fn skip_whitespace(&mut self) {
        while self.is_whitespace() {
            self.read();
        }
    }

    fn read(&mut self) {
        if '\n' == self.current {
            self.line += 1;
            self.column = 0;
        }
        self.index += 1;
        #[allow(clippy::cast_possible_wrap, clippy::cast_sign_loss)]
        if self.index < self.input.len() as isize {
            self.current = self.input.as_bytes()[self.index as usize] as char;
        } else {
            self.current = 0 as char;
        }
    }

    fn start_capture(&mut self) {
        self.capture_start = self.index;
    }

    fn pause_capture(&mut self) {
        let end = if self.current == 0 as char {
            self.index
        } else {
            self.index - 1
        };
        #[allow(clippy::cast_possible_wrap, clippy::cast_sign_loss)]
        for &c in &self.input.as_bytes()[self.capture_start as usize..=end as usize] {
            self.capture_buffer.push(c as char);
        }
        self.capture_start = -1;
    }

    fn end_capture(&mut self) -> String {
        let end = if self.current == 0 as char {
            self.index
        } else {
            self.index - 1
        };
        #[allow(clippy::cast_possible_wrap, clippy::cast_sign_loss)]
        for &c in &self.input.as_bytes()[self.capture_start as usize..=end as usize] {
            self.capture_buffer.push(c as char);
        }
        let mut captured = String::new();
        std::mem::swap(&mut captured, &mut self.capture_buffer);
        self.capture_start = -1;
        captured
    }

    fn expected<T>(&self, expected: impl Display) -> JsonParseResult<T> {
        if self.is_end_of_text() {
            self.error("Unexpected end of input")
        } else {
            self.error(format!("Expected {expected}"))
        }
    }

    fn error<T>(&self, message: impl Display) -> JsonParseResult<T> {
        Err(ParseException::new(
            message.to_string(),
            self.index,
            self.line,
            self.column - 1,
        ))
    }

    fn is_whitespace(&self) -> bool {
        self.current == ' ' || self.current == '\t' || self.current == '\n' || self.current == '\r'
    }

    fn is_digit(&self) -> bool {
        '0' == self.current
            || '1' == self.current
            || '2' == self.current
            || '3' == self.current
            || '4' == self.current
            || '5' == self.current
            || '6' == self.current
            || '7' == self.current
            || '8' == self.current
            || '9' == self.current
    }

    fn is_end_of_text(&self) -> bool {
        self.current == 0 as char
    }
}
