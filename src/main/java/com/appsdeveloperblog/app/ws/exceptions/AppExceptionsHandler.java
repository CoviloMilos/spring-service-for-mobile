package com.appsdeveloperblog.app.ws.exceptions;

import java.util.Date;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import com.appsdeveloperblog.app.ws.ui.model.response.ErrorMessage;

@ControllerAdvice
public class AppExceptionsHandler {

	// Handle multiple excpetions using one method by adding one more class of exception
	@ExceptionHandler(value= {UserServiceException.class})
	public ResponseEntity<Object> handleUserServiceException(UserServiceException ex, WebRequest request) {
		ErrorMessage errorMsg = new ErrorMessage(new Date(), ex.getMessage());
		return new ResponseEntity<>(errorMsg, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@ExceptionHandler(value= {Exception.class})
	public ResponseEntity<Object> handleOtherExceptions(Exception ex, WebRequest request) {
		ErrorMessage errorMsg = new ErrorMessage(new Date(), ex.getMessage());
		return new ResponseEntity<>(errorMsg, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
