#[derive(Debug)]
struct LineNumberItem {
    line: usize,
    count: usize,
}

#[derive(Debug)]
pub struct LineNumber {
    list: Vec<LineNumberItem>,
}

impl LineNumber {
    pub fn new() -> LineNumber {
        LineNumber { list: Vec::new() }
    }

    pub fn add_line(&mut self, line: usize) {
        let current_line = self.current_line();
        let list = &mut self.list;
        let list_len = list.len();

        if line <= 0 {
            panic!("Line number must be bigger then 0")
        }

        if current_line > line {
            panic!("Line number should monotonically increasing number")
        }

        if list_len == 0 || current_line != line {
            list.push(LineNumberItem { line, count: 1 })
        } else {
            let item = &mut list[list_len - 1];
            item.count += 1;
        }
    }

    pub fn get_line(&self, chunk_idx: usize) -> usize {
        let list = &self.list;
        if list.len() == 0 {
            return 0;
        }
        let mut i = 0;
        let mut c = list[0].count - 1;
        while c < chunk_idx && i < list.len() {
            i = i + 1;
            c = c + list[i].count;
        }
        list[i].line
    }

    fn current_line(&self) -> usize {
        let list = &self.list;
        match list.len() {
            0 => 0,
            n => list[n - 1].line,
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_init() {
        let ln = LineNumber::new();
        assert_eq!(ln.get_line(42), 0)
    }

    #[test]
    #[should_panic(expected = "Line number must be bigger then 0")]
    fn test_add_line_zero() {
        let mut ln = LineNumber::new();
        ln.add_line(0);
    }

    #[test]
    #[should_panic(expected = "Line number should monotonically increasing number")]
    fn test_add_line_in_random_order() {
        let mut ln = LineNumber::new();
        ln.add_line(10);
        ln.add_line(1);
    }

    #[test]
    fn test_add_lines() {
        let mut ln = LineNumber::new();
        ln.add_line(1);
        ln.add_line(1);
        ln.add_line(2);
        ln.add_line(3);
        ln.add_line(3);
        ln.add_line(3);
        ln.add_line(42);

        assert_eq!(ln.get_line(0), 1);
        assert_eq!(ln.get_line(1), 1);
        assert_eq!(ln.get_line(2), 2);
        assert_eq!(ln.get_line(3), 3);
        assert_eq!(ln.get_line(4), 3);
        assert_eq!(ln.get_line(5), 3);
        assert_eq!(ln.get_line(6), 42);
    }
}
