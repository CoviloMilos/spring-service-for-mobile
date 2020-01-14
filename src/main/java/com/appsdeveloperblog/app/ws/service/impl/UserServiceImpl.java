package com.appsdeveloperblog.app.ws.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.appsdeveloperblog.app.ws.exceptions.UserServiceException;
import com.appsdeveloperblog.app.ws.io.entity.UserEntity;
import com.appsdeveloperblog.app.ws.io.repositories.UserRepository;
import com.appsdeveloperblog.app.ws.service.UserService;
import com.appsdeveloperblog.app.ws.shared.dto.UserDto;
import com.appsdeveloperblog.app.ws.shared.utils.Utils;
import com.appsdeveloperblog.app.ws.ui.model.response.ErrorMessages;

@Service
public class UserServiceImpl implements UserService{
	
	@Autowired
	UserRepository userRepo;
	
	@Autowired
	Utils utils;
	
	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Override
	public UserDto createUser(UserDto user) {
		
		if(userRepo.findByEmail(user.getEmail()) != null) throw new RuntimeException("Record already exists");
		
		UserEntity userEntity = new UserEntity();
		BeanUtils.copyProperties(user, userEntity);
		
		String publicUserId = utils.generateUserId(30);
		userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(user.getPassword()));
		userEntity.setUserId(publicUserId);
		
		UserEntity storedUserDeatils = userRepo.save(userEntity);
		
		UserDto returnValue = new UserDto();
		BeanUtils.copyProperties(storedUserDeatils, returnValue);
		
		return returnValue;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		UserEntity userEntity = userRepo.findByEmail(email);
		
		if (userEntity == null) throw new UsernameNotFoundException(email);
		
		return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), new ArrayList<>());
	}

	@Override
	public UserDto getUser(String email) {
		UserEntity userEntity = userRepo.findByEmail(email);
		
		if (userEntity == null) throw new UsernameNotFoundException(email);
		
		UserDto returnValue = new UserDto();
		BeanUtils.copyProperties(userEntity, returnValue);
		
		return returnValue;
	}

	@Override
	public UserDto getUserByUserId(String id) {
		UserEntity userEntity = userRepo.findByUserId(id);
		
		if (userEntity == null) throw new UsernameNotFoundException("User with ID: " + id + " not found");
		
		UserDto returnValue = new UserDto();
		BeanUtils.copyProperties(userEntity, returnValue);
		
		return returnValue;
	}

	@Override
	public UserDto updateUser(String id, UserDto userDto) {
		UserDto returnValue = new UserDto();
		UserEntity userEntity = userRepo.findByUserId(id);
		
		if (userEntity == null) throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
		
		userEntity.setFirstName(userDto.getFirstName());
		userEntity.setLastName(userDto.getLastName());
		
		UserEntity updatedUserDetails = userRepo.save(userEntity);
		BeanUtils.copyProperties(updatedUserDetails, returnValue);
		
		return returnValue;
	}

	@Override
	public void deleteUser(String id) {
		UserEntity userEntity = userRepo.findByUserId(id);
		
		if (userEntity == null) throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
		
		userRepo.delete(userEntity);
	}

	@Override
	public List<UserDto> getUsers(int page, int limit) {
		List<UserDto> returnValue = new ArrayList<>();
		
		Pageable pageableRequest = PageRequest.of(page, limit);
		Page<UserEntity> usersPage = userRepo.findAll(pageableRequest);
		List<UserEntity> users = usersPage.getContent();
		
		for (UserEntity userEntity : users) {
			UserDto userDto = new UserDto();
			BeanUtils.copyProperties(userEntity, userDto);
			returnValue.add(userDto);
		}
		
		return returnValue;
	}

}
