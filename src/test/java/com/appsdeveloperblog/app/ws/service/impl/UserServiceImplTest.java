package com.appsdeveloperblog.app.ws.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.appsdeveloperblog.app.ws.exceptions.UserServiceException;
import com.appsdeveloperblog.app.ws.io.entity.AddressEntity;
import com.appsdeveloperblog.app.ws.io.entity.UserEntity;
import com.appsdeveloperblog.app.ws.io.repositories.UserRepository;
import com.appsdeveloperblog.app.ws.shared.dto.AddressDto;
import com.appsdeveloperblog.app.ws.shared.dto.UserDto;
import com.appsdeveloperblog.app.ws.shared.utils.Utils;

import java.lang.reflect.Type;
import org.modelmapper.TypeToken;

class UserServiceImplTest {

	@InjectMocks
	UserServiceImpl userService;
	
	@Mock
	UserRepository userRepo;
	
	@Mock
	Utils utils;
	
	@Mock
	BCryptPasswordEncoder bCryptPasswordEncoder;
	
	String userId = "74ryryY1FFa";
	String encryptedPassword = "encPass";
	
	UserEntity userEntity;
	
	@BeforeEach
	void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		userEntity = new UserEntity();
		userEntity.setId(1L);
		userEntity.setFirstName("First");
		userEntity.setLastName("Last");
		userEntity.setUserId(userId);
		userEntity.setEncryptedPassword(encryptedPassword);
		userEntity.setEmail("test@test.com");
		userEntity.setAddresses(getAddressesEntity());
	}

	@Test
	void testGetUser() {
		when(userRepo.findByEmail(Mockito.<String>any())).thenReturn(userEntity);
		
		UserDto userDto = userService.getUser("test@test.com");
		
		assertNotNull(userDto);
		assertEquals("First", userDto.getFirstName());
	}
	
	@Test
	final void testGetUser_UsernameNotFoundException() {	
		when(userRepo.findByEmail(Mockito.<String>any())).thenReturn(null);
		
		assertThrows(UsernameNotFoundException.class, 
				() -> {
					userService.getUser("test@test.com");
				});
	}
	
	@Test
	final void testCreateUser() {
		when(userRepo.findByEmail(Mockito.<String>any())).thenReturn(null);
		when(utils.generateAddressId(30)).thenReturn("hhh123fs^kgeEE");
		when(utils.generateUserId(11)).thenReturn(userId);
		when(bCryptPasswordEncoder.encode(Mockito.<String>any())).thenReturn(encryptedPassword);
		when(userRepo.save(Mockito.<UserEntity>any())).thenReturn(userEntity);
		
		UserDto userDto = new UserDto();
		userDto.setId(1L);
		userDto.setFirstName("First");
		userDto.setLastName("Last");
		userDto.setUserId(userId);
		userDto.setEncryptedPassword(encryptedPassword);
		userDto.setEmail("test@test.com");
		userDto.setAddresses(getAddressesDto());
		
		
		UserDto storedUserDetails = userService.createUser(userDto);
		
		assertNotNull(storedUserDetails);
		assertEquals(userEntity.getFirstName(), storedUserDetails.getFirstName());
		assertEquals(userEntity.getLastName(), storedUserDetails.getLastName());
		assertNotNull(storedUserDetails.getUserId());
		assertEquals(storedUserDetails.getAddresses().size(), userEntity.getAddresses().size());
		verify(userRepo, times(1)).save(Mockito.<UserEntity>any());
	}
	
	@Test
	final void testCreateUser_UserServiceException() {	
		when(userRepo.findByEmail(Mockito.<String>any())).thenReturn(userEntity);
		
		UserDto userDto = new UserDto();
		userDto.setId(1L);
		userDto.setFirstName("First");
		userDto.setLastName("Last");
		userDto.setUserId(userId);
		userDto.setEncryptedPassword(encryptedPassword);
		userDto.setEmail("test@test.com");
		userDto.setAddresses(getAddressesDto());
		
		assertThrows(UserServiceException.class, 
				() -> {
					userService.createUser(userDto);
				});
	}
	
	
	private List<AddressDto> getAddressesDto() {
		AddressDto addressDto = new AddressDto();
		addressDto.setType("shipping");
		addressDto.setCity("Vancouver");
		addressDto.setCountry("Canada");
		addressDto.setPostalCode("ABC123");
		addressDto.setStreetName("123 Street name");

		AddressDto billingAddressDto = new AddressDto();
		billingAddressDto.setType("billling");
		billingAddressDto.setCity("Vancouver");
		billingAddressDto.setCountry("Canada");
		billingAddressDto.setPostalCode("ABC123");
		billingAddressDto.setStreetName("123 Street name");

		List<AddressDto> addresses = new ArrayList<>();
		addresses.add(addressDto);
		addresses.add(billingAddressDto);

		return addresses;

	}
	
	private List<AddressEntity> getAddressesEntity()
	{
		List<AddressDto> addresses = getAddressesDto();
		
	    Type listType = new TypeToken<List<AddressEntity>>() {}.getType();
	    
	    return new ModelMapper().map(addresses, listType);
	}

}
