package com.example.demo;

public interface AttributeConverter {
	public byte[] convertToDatabaseColumn(String atributo);
	public String convertToEntityAttribute(byte[] dbData);
}
