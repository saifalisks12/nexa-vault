package com.nexavault.config;

import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for creating and configuring a ModelMapper bean.
 * ModelMapper is a flexible Java library for mapping objects between different
 * Java bean types.
 */
@Configuration
public class Mapper {

    /**
     * Creates and configures a ModelMapper bean with specific settings.
     * 
     * @return Configured ModelMapper bean for object mapping.
     */
    @Bean
    ModelMapper modelMapper() {
		ModelMapper modelMapper = new ModelMapper();

		// Configuring ModelMapper settings
		modelMapper.getConfiguration().setSkipNullEnabled(true) // Enable skipping null properties during mapping
				.setMatchingStrategy(MatchingStrategies.STRICT) // Use strict matching strategy for precise mapping
				.setPropertyCondition(Conditions.isNotNull()); // Skip mapping for null property values

		return modelMapper;
	}
}
