package io.github.pdkst.autocompletelog;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Sample {
    public void test(String name, int age) {
        log.info("test => test in, ", name);
    }
}
