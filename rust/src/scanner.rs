#[derive(Copy, Clone, Debug, Eq, PartialEq)]
enum TT {
    // Single-character tokens.
    LeftParen,
    RightParen,
    LeftBrace,
    RightBrace,
    Comma,
    Dot,
    Minus,
    Plus,
    Semicolon,
    Slash,
    Star,

    // One or two character tokens.
    Bang,
    BangEqual,
    Equal,
    EqualEqual,
    Greater,
    GreaterEqual,
    Less,
    LessEqual,

    // Literals.
    Identifier,
    String,
    Number,

    // Keywords.
    And,
    Class,
    Else,
    False,
    For,
    Fun,
    If,
    Nil,
    Or,
    Print,
    Return,
    Super,
    This,
    True,
    Var,
    While,

    Error,
    Eof,
}

pub struct Token {
    typ: TT,
    data: *const str,
    line: usize,
}

pub struct Scanner<'a> {
    source: &'a str,
    start: usize,
    current: usize,
    line: usize,
}

fn is_digit(c: char) -> bool {
    c >= '0' && c <= '9'
}

fn is_alpha(c: char) -> bool {
    (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_'
}

impl Scanner<'_> {
    pub fn new(source: &str) -> Scanner {
        Scanner {
            source: &source,
            start: 0,
            current: 0,
            line: 1,
        }
    }

    pub fn scan_token(&mut self) -> Token {
        self.skip_whitespace();
        self.start = self.current;

        if self.is_at_end() {
            return self.make_token(TT::Eof);
        }

        let c = self.advance();

        return match c {
            '(' => self.make_token(TT::LeftParen),
            ')' => self.make_token(TT::RightParen),
            '{' => self.make_token(TT::LeftBrace),
            '}' => self.make_token(TT::RightBrace),
            ';' => self.make_token(TT::Semicolon),
            ',' => self.make_token(TT::Comma),
            '.' => self.make_token(TT::Dot),
            '-' => self.make_token(TT::Minus),
            '+' => self.make_token(TT::Plus),
            '/' => self.make_token(TT::Slash),
            '*' => self.make_token(TT::Star),
            '!' => self.long_op_token('=', TT::BangEqual, TT::Bang),
            '=' => self.long_op_token('=', TT::EqualEqual, TT::Equal),
            '<' => self.long_op_token('=', TT::LessEqual, TT::Less),
            '>' => self.long_op_token('=', TT::GreaterEqual, TT::Greater),
            '"' => self.string_token(),
            '0'..='9' => self.number_token(),
            is_alpha => self.identifier_token(),
            _ => self.error_token("Unexpected character."),
        };
    }

    fn get_char(&mut self, idx: usize) -> char {
        self.source.chars().nth(idx).unwrap()
    }

    fn peek(&mut self) -> char {
        if self.is_at_end() {
            return '\0';
        }
        self.get_char(self.current)
    }

    fn peek_next(&mut self) -> char {
        if self.is_at_end() {
            return '\0';
        }
        self.get_char(self.current + 1)
    }

    fn advance(&mut self) -> char {
        self.current += 1;
        self.get_char(self.current - 1)
    }

    fn either(&mut self, expected: char, opt_a: TT, opt_b: TT) -> TT {
        if self.is_at_end() || self.peek() != expected {
            return opt_b;
        }
        self.current += 1;
        return opt_a;
    }

    fn skip_whitespace(&mut self) {
        loop {
            match self.peek() {
                ' ' | '\r' | '\t' => {
                    self.advance();
                }
                '\n' => {
                    self.new_line();
                }
                '/' => {
                    if self.peek_next() == '/' {
                        while self.peek() != '\n' && !self.is_at_end() {
                            self.advance();
                        }
                    } else {
                        return;
                    }
                }
                _ => return,
            }
        }
    }

    fn make_token(&self, typ: TT) -> Token {
        Token {
            typ,
            data: &self.source[self.start..self.current],
            line: self.line,
        }
    }

    fn error_token(&self, msg: &str) -> Token {
        Token {
            typ: TT::Error,
            data: msg,
            line: self.line,
        }
    }

    fn long_op_token(&mut self, expected: char, opt_a: TT, opt_b: TT) -> Token {
        let token_type = self.either(expected, opt_a, opt_b);
        return self.make_token(token_type);
    }

    fn string_token(&mut self) -> Token {
        while self.peek() != '"' {
            if self.peek() == '\n' {
                self.new_line()
            }
            if self.is_at_end() {
                return self.error_token("Unterminated string.");
            }
            self.advance();
        }
        // The closing quote.
        self.advance();
        return self.make_token(TT::String);
    }

    fn number_token(&mut self) -> Token {
        while is_digit(self.peek()) {
            self.advance();
        }

        // Look for a fractional part.
        if self.peek() == '.' && is_digit(self.peek_next()) {
            // Consume the ".".
            self.advance();

            while is_digit(self.peek()) {
                self.advance();
            }
        }

        return self.make_token(TT::Number);
    }

    fn identifier_token(&mut self) -> Token {
        while is_alpha(self.peek()) || is_digit(self.peek()) {
            self.advance();
        }

        return self.make_token(self.identifier_type());
    }

    fn identifier_type(&mut self) -> TT {
        return match self.get_char(self.start) {
            'a' => self.check_keyword(1, 2, "nd", TT::And),
            'c' => self.check_keyword(1, 4, "lass", TT::Class),
            'e' => self.check_keyword(1, 3, "lse", TT::Else),
            'i' => self.check_keyword(1, 1, "f", TT::If),
            'n' => self.check_keyword(1, 2, "il", TT::Nil),
            'o' => self.check_keyword(1, 1, "r", TT::Or),
            'p' => self.check_keyword(1, 4, "rint", TT::Print),
            'r' => self.check_keyword(1, 5, "eturn", TT::Return),
            's' => self.check_keyword(1, 4, "uper", TT::Super),
            'v' => self.check_keyword(1, 2, "ar", TT::Var),
            'w' => self.check_keyword(1, 4, "hile", TT::While),
            _ => TT::Identifier,
        };
    }

    fn check_keyword(&mut self, start: usize, len: usize, rest: &str, tt: TT) -> TT {
        if &self.source[self.start + start..len] == rest {
            tt
        } else {
            TT::Identifier
        }
    }

    fn is_at_end(&self) -> bool {
        self.current == self.source.chars().count()
    }

    fn new_line(&mut self) {
        self.line += 1;
        self.advance();
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn check_basics() {
        let mut s = Scanner::new(&"(){};,.-+/*");

        assert_eq!(s.scan_token().typ, TT::LeftParen);
        assert_eq!(s.scan_token().typ, TT::RightParen);
        assert_eq!(s.scan_token().typ, TT::LeftBrace);
        assert_eq!(s.scan_token().typ, TT::RightBrace);
        assert_eq!(s.scan_token().typ, TT::Semicolon);
        assert_eq!(s.scan_token().typ, TT::Comma);
        assert_eq!(s.scan_token().typ, TT::Dot);
        assert_eq!(s.scan_token().typ, TT::Minus);
        assert_eq!(s.scan_token().typ, TT::Plus);
        assert_eq!(s.scan_token().typ, TT::Slash);
        assert_eq!(s.scan_token().typ, TT::Star);
        assert_eq!(s.scan_token().typ, TT::Eof);
    }

    #[test]
    fn check_long_ops() {
        let mut s = Scanner::new(&"! != = == < <= > >= ");

        assert_eq!(s.scan_token().typ, TT::Bang);
        assert_eq!(s.scan_token().typ, TT::BangEqual);
        assert_eq!(s.scan_token().typ, TT::Equal);
        assert_eq!(s.scan_token().typ, TT::EqualEqual);
        assert_eq!(s.scan_token().typ, TT::Less);
        assert_eq!(s.scan_token().typ, TT::LessEqual);
        assert_eq!(s.scan_token().typ, TT::Greater);
        assert_eq!(s.scan_token().typ, TT::GreaterEqual);
        assert_eq!(s.scan_token().typ, TT::Eof);
    }

    #[test]
    fn check_whitespace() {
        let mut s = Scanner::new(&" \t\n");

        assert_eq!(s.scan_token().typ, TT::Eof);
    }

    #[test]
    fn check_single_line_comment() {
        let mut s = Scanner::new(&"; // hello world\n ;");

        assert_eq!(s.scan_token().typ, TT::Semicolon);
        assert_eq!(s.scan_token().typ, TT::Semicolon);
        assert_eq!(s.scan_token().typ, TT::Eof);
    }

    #[test]
    fn check_string() {
        let mut s = Scanner::new(&r###";"hello \n worl";"###);

        assert_eq!(s.scan_token().typ, TT::Semicolon);
        assert_eq!(s.scan_token().typ, TT::String);
        assert_eq!(s.scan_token().typ, TT::Semicolon);
        assert_eq!(s.scan_token().typ, TT::Eof);
    }

    #[test]
    fn check_invalid_string() {
        let expected_error = "Unterminated string.";
        let mut s = Scanner::new(&r###""hello "###);
        assert_eq!(s.scan_token().typ, TT::Error);
        let error = s.scan_token().data;
    }

    #[test]
    fn check_number() {
        let mut s = Scanner::new(&"123");

        assert_eq!(s.scan_token().typ, TT::Number);
        assert_eq!(s.scan_token().typ, TT::Eof);

        let mut s2 = Scanner::new(&"123.42");

        assert_eq!(s2.scan_token().typ, TT::Number);
        assert_eq!(s2.scan_token().typ, TT::Eof);
    }
}
