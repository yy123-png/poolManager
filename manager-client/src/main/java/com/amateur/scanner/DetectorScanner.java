package com.amateur.scanner;

import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author yeyu
 * @date 2021/12/14 17:30
 */
@Component
public class DetectorScanner extends AbstractScanner {

    @Override
    public Set<Class<?>> doScan() {
        return null;
    }

    @Override
    public boolean check(Class<?> clazz) {
        return false;
    }

}
