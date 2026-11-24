package cl.duoc.auraarriendos.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;

/**
 * Convierte una lista de textos (la galería de fotos) a JSON para guardarla
 * en una sola columna, y de vuelta a lista al leerla.
 */
@Converter
public class ListaJsonConverter implements AttributeConverter<List<String>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<String> lista) {
        try {
            return MAPPER.writeValueAsString(lista == null ? List.of() : lista);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo guardar la lista de imágenes", e);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return new ArrayList<>(MAPPER.readValue(json, new TypeReference<List<String>>() { }));
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }
}
