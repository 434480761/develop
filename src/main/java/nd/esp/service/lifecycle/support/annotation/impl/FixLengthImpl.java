package nd.esp.service.lifecycle.support.annotation.impl;



import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import nd.esp.service.lifecycle.support.annotation.FixLength;

public class FixLengthImpl implements ConstraintValidator<FixLength, String> {  
    private int length;  
    @Override  
    public boolean isValid(String validStr,  
            ConstraintValidatorContext constraintContext) {  
        if (validStr.length() != length) {  
            return false;  
        } else {  
            return true;  
        }  
    }  
  
    @Override  
    public void initialize(FixLength fixLen) {  
        this.length = fixLen.length();  
    }  
}  