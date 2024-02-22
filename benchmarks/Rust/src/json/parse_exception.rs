use std::error::Error;
use std::fmt::Display;

#[derive(Debug)]
pub struct ParseException {
    message: String,
    offset: isize,
    line: usize,
    column: usize,
}

impl ParseException {
    pub fn new(message: String, offset: isize, line: usize, column: usize) -> ParseException {
        ParseException {
            message,
            offset,
            line,
            column,
        }
    }

    pub fn get_offset(&self) -> isize {
        self.offset
    }

    pub fn get_line(&self) -> usize {
        self.line
    }

    pub fn get_column(&self) -> usize {
        self.column
    }
}

impl Display for ParseException {
    fn fmt(&self, f: &mut std::fmt::Formatter) -> std::fmt::Result {
        let Self {
            message,
            line,
            column,
            ..
        } = self;
        write!(f, "{message} at {line}:{column}")
    }
}

impl Error for ParseException {}
