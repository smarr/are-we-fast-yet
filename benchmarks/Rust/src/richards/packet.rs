use crate::richards::rb_object::RBObject;
use std::fmt::{Display, Formatter};

pub struct Packet {
    link: Option<PacketBox>,
    identity: usize,
    kind: usize,
    datum: usize,
    data: [usize; 4],
}

#[allow(clippy::module_name_repetitions)]
pub type PacketBox = Box<Packet>;

impl RBObject for Packet {}

impl Packet {
    pub const DATA_SIZE: usize = 4;

    pub fn new(link: Option<PacketBox>, identity: usize, kind: usize) -> Packet {
        Packet {
            link,
            identity: identity,
            kind,
            datum: 0,
            data: [0; Packet::DATA_SIZE],
        }
    }

    pub fn new_boxed(link: Option<PacketBox>, identity: usize, kind: usize) -> PacketBox {
        Box::new(Packet::new(link, identity, kind))
    }

    pub fn get_data(&self) -> &[usize; Packet::DATA_SIZE] {
        &self.data
    }

    pub fn get_data_mut(&mut self) -> &mut [usize; Packet::DATA_SIZE] {
        &mut self.data
    }

    pub fn get_datum(&self) -> usize {
        self.datum
    }

    pub fn set_datum(&mut self, some_data: usize) {
        self.datum = some_data;
    }

    pub fn get_identity(&self) -> usize {
        self.identity
    }

    pub fn set_identity(&mut self, an_identity: usize) {
        self.identity = an_identity;
    }

    pub fn get_kind(&self) -> usize {
        self.kind
    }

    #[allow(dead_code)]
    pub fn get_link(&self) -> &Option<PacketBox> {
        &self.link
    }

    pub fn take_link(&mut self) -> Option<PacketBox> {
        self.link.take()
    }

    pub fn set_link(&mut self, a_link: Option<PacketBox>) {
        self.link = a_link;
    }

    pub fn append_link(&mut self, a_link: Option<PacketBox>) {
        let mut link = &mut self.link;
        while let Some(p) = link {
            link = &mut p.link;
        }
        *link = a_link;
    }
}

impl Display for Packet {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        let Self { identity, kind, .. } = self;
        write!(f, "Packet id: {identity} kind: {kind}")
    }
}
