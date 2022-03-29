#[derive(Clone, Debug)]
pub enum JsonValue {
    Literal {
        value: &'static str,
        is_null: bool,
        is_true: bool,
        is_false: bool,
    },
    Number(String),
    String(String),
    Array(Vec<JsonValue>),
    Object(JsonObject),
}

#[derive(Clone, Debug, Default)]
pub struct JsonObject {
    names: Vec<String>,
    values: Vec<JsonValue>,
    table: HashIndexTable,
}

impl JsonValue {
    pub const NULL: JsonValue = JsonValue::Literal {
        value: "null",
        is_null: true,
        is_true: false,
        is_false: false,
    };
    pub const TRUE: JsonValue = JsonValue::Literal {
        value: "true",
        is_null: false,
        is_true: true,
        is_false: false,
    };
    pub const FALSE: JsonValue = JsonValue::Literal {
        value: "false",
        is_null: false,
        is_true: false,
        is_false: true,
    };

    pub fn as_string(&self) -> Option<&str> {
        match self {
            JsonValue::Literal { value, .. } => Some(value),
            JsonValue::Number(string) => Some(string),
            JsonValue::String(string) => Some(string),
            JsonValue::Array(_) => None,
            JsonValue::Object { .. } => None,
        }
    }

    pub fn is_null(&self) -> bool {
        if let JsonValue::Literal { is_null, .. } = self {
            *is_null
        } else {
            false
        }
    }

    pub fn is_false(&self) -> bool {
        if let JsonValue::Literal { is_false, .. } = self {
            *is_false
        } else {
            false
        }
    }

    pub fn is_true(&self) -> bool {
        if let JsonValue::Literal { is_true, .. } = self {
            *is_true
        } else {
            false
        }
    }

    pub fn is_boolean(&self) -> bool {
        if let JsonValue::Literal {
            is_true, is_false, ..
        } = self
        {
            *is_true || *is_false
        } else {
            false
        }
    }

    pub fn is_number(&self) -> bool {
        if let JsonValue::Number { .. } = self {
            true
        } else {
            false
        }
    }

    pub fn is_string(&self) -> bool {
        if let JsonValue::String(..) = self {
            true
        } else {
            false
        }
    }

    pub fn is_array(&self) -> bool {
        if let JsonValue::Array(..) = self {
            true
        } else {
            false
        }
    }

    pub fn as_array(&self) -> &Vec<JsonValue> {
        match self {
            JsonValue::Array(array) => array,
            _ => panic!("Not an Array: {self:?}"),
        }
    }

    pub fn as_array_mut(&mut self) -> &mut Vec<JsonValue> {
        match self {
            JsonValue::Array(array) => array,
            _ => panic!("Not an Array: {self:?}"),
        }
    }

    pub fn is_object(&self) -> bool {
        if let JsonValue::Object(..) = self {
            true
        } else {
            false
        }
    }

    pub fn as_object(&self) -> &JsonObject {
        match self {
            JsonValue::Object(object) => object,
            _ => panic!("Not an object: {self:?}"),
        }
    }

    pub fn as_object_mut(&mut self) -> &mut JsonObject {
        match self {
            JsonValue::Object(object) => object,
            _ => panic!("Not an object: {self:?}"),
        }
    }
}

impl JsonObject {
    pub fn add(&mut self, name: String, value: JsonValue) {
        self.table.add(name.clone(), self.names.len() as isize);
        self.names.push(name);
        self.values.push(value);
    }

    pub fn get(&self, name: &str) -> Option<&JsonValue> {
        let index = self.index_of(name)?;
        Some(&self.values[index])
    }

    pub fn get_mut(&mut self, name: &str) -> Option<&mut JsonValue> {
        let index = self.index_of(name)?;
        Some(&mut self.values[index])
    }

    pub fn len(&self) -> usize {
        self.names.len()
    }

    pub fn is_empty(&self) -> bool {
        self.names.is_empty()
    }

    pub fn index_of(&self, name: &str) -> Option<usize> {
        let index: usize = self.table.get(name).try_into().ok()?;
        if name == self.names[index as usize] {
            Some(index as usize)
        } else {
            None
        }
    }
}

#[derive(Clone, Debug, Default)]
struct HashIndexTable {
    hash_table: [isize; 32],
}

impl HashIndexTable {
    fn add(&mut self, name: String, index: isize) {
        let slot = self.hash_slot_for(&name);
        if index < 0xff {
            self.hash_table[slot as usize] = (index + 1) & 0xff;
        } else {
            self.hash_table[slot as usize] = 0;
        }
    }

    fn get(&self, name: &str) -> isize {
        let slot = self.hash_slot_for(name);
        (self.hash_table[slot as usize] & 0xff) - 1
    }

    fn string_hash(s: &str) -> isize {
        // this is not a proper hash, but sufficient for the benchmark,
        // and very portable!
        s.len() as isize * 1_402_589
    }

    fn hash_slot_for(&self, name: &str) -> isize {
        Self::string_hash(name) & (self.hash_table.len() as isize - 1)
    }
}
