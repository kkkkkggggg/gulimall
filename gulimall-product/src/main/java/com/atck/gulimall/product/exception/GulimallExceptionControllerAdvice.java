package com.atck.gulimall.product.exception;

import com.atck.common.exception.BizCodeEnume;
import com.atck.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 集中处理所有异常
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.atck.gulimall.product.controller")
public class GulimallExceptionControllerAdvice
{

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e)
    {
        log.error("数据校验出现问题{},异常类型:{}",e.getMessage(),e.getClass());
        Map<String,String> map = new HashMap<>();
        BindingResult result = e.getBindingResult();

        result.getFieldErrors().forEach((item) ->{
            String message = item.getDefaultMessage();
            String field = item.getField();
            map.put(field,message);
        });
        return R.error(BizCodeEnume.VALID_EXCEPTION.getCode(),BizCodeEnume.VALID_EXCEPTION.getMsg()).put("data",map);
    }

    // @ExceptionHandler(Throwable.class)
    // public R handleException(Throwable throwable)
    // {
    //     return R.error(BizCodeEnume.UNKONOW_EXCEPTION.getCode(),BizCodeEnume.UNKONOW_EXCEPTION.getMsg());
    // }
}
