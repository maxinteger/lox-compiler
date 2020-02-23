use crate::scanner::Scanner;

pub fn compile(source: &str) {
    let mut scanner = Scanner::new(&source);

    loop {
        let _token = scanner.scan_token();
    }
    //    int line = -1;
    //    for (;;) {
    //        Token token = scanToken();
    //        if (token.line != line) {
    //            printf("%4d ", token.line);
    //            line = token.line;
    //        } else {
    //            printf("   | ");
    //        }
    //        printf("%2d '%.*s'\n", token.type, token.length, token.start); ​
    //
    //        if (token.type == TOKEN_EOF) break;
    //    }
}
