package com.appsdeveloperblog.app.ws.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.modelmapper.ModelMapper;
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
import com.appsdeveloperblog.app.ws.io.entity.PasswordResetTokenEntity;
import com.appsdeveloperblog.app.ws.io.entity.UserEntity;
import com.appsdeveloperblog.app.ws.io.repositories.PasswordResetTokenRepository;
import com.appsdeveloperblog.app.ws.io.repositories.UserRepository;
import com.appsdeveloperblog.app.ws.service.UserService;
import com.appsdeveloperblog.app.ws.shared.dto.AddressDto;
import com.appsdeveloperblog.app.ws.shared.dto.UserDto;
import com.appsdeveloperblog.app.ws.shared.utils.Utils;
import com.appsdeveloperblog.app.ws.ui.model.response.ErrorMessages;

@Service
public class UserServiceImpl implements UserService{
	
	@Autowired
	UserRepository userRepo;
	
	@Autowired
	PasswordResetTokenRepository passwordResetTokenRepo;
	
	@Autowired
	Utils utils;
	
	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	ModelMapper modelMapper;
	
	@Override
	public UserDto createUser(UserDto user) {
		
		if(userRepo.findByEmail(user.getEmail()) != null) throw new UserServiceException("Record already exists");
		
		for (int i = 0; i < user.getAddresses().size(); i++) {
			AddressDto address = user.getAddresses().get(i);
			address.setUserDetails(user);
			address.setAddressId(utils.generateAddressId(30));
			user.getAddresses().set(i, address);
		}
		ModelMapper modelMapperTst = new ModelMapper();
		UserEntity userEntity = modelMapperTst.map(user, UserEntity.class);
		
		String publicUserId = utils.generateUserId(30);
		userEntity.setUserId(publicUserId);
		userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(user.getPassword()));
		
		UserEntity storedUserDeatils = userRepo.save(userEntity);
		
		return modelMapperTst.map(storedUserDeatils, UserDto.class);
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

		return modelMapper.map(userEntity, UserDto.class);
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

	@Transactional
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

	@Override
	public boolean requestPasswordReset(String email) {		
		UserEntity userEntity = userRepo.findByEmail(email);
		
		if (userEntity == null) {
			return false;
		}
		
		String token = utils.generatePasswordResetToken(userEntity.getUserId());
		
		PasswordResetTokenEntity passwordResetTokenEntity = new PasswordResetTokenEntity();
		passwordResetTokenEntity.setToken(token);
		passwordResetTokenEntity.setUserDetails(userEntity);
		try {
			passwordResetTokenRepo.save(passwordResetTokenEntity);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean resetPassword(String token, String password) {
		boolean returnValue = false;
		
		if (utils.hasTokenExpired(token)) {
			return returnValue;
		}
		
		PasswordResetTokenEntity passwordResetTokenEntity = passwordResetTokenRepo.findByToken(token);
		
		if (passwordResetTokenEntity == null) {
			return returnValue;
		}
		
		String encodedPassword = bCryptPasswordEncoder.encode(password);
		
		UserEntity userEntity = passwordResetTokenEntity.getUserDetails();
        userEntity.setEncryptedPassword(encodedPassword);
        UserEntity savedUserEntity = userRepo.save(userEntity);
        
        if (savedUserEntity != null && savedUserEntity.getEncryptedPassword().equalsIgnoreCase(encodedPassword)) {
        	returnValue = true;
        }
        
        passwordResetTokenRepo.delete(passwordResetTokenEntity);
        
		return returnValue;
	}

}
