package com.udea.supermercado.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.udea.supermercado.dto.ClienteDto;
import com.udea.supermercado.exception.ReglaNegocioException;
import com.udea.supermercado.model.Cliente;
import com.udea.supermercado.repository.ClienteRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Validaciones de clientes: el servidor no confia en los datos que envia el modelo. */
@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {
    @Mock ClienteRepository clientes;
    @InjectMocks ClienteService servicio;

    @Test
    void registrarGuardaLosDatosLimpiosYNormalizaElTelefono() {
        when(clientes.existsByTelefono("3001234567")).thenReturn(false);
        when(clientes.save(any(Cliente.class))).thenAnswer(i -> i.getArgument(0));

        ClienteDto dto = servicio.registrar("  Ana Ruiz ", "300 123-4567", " Calle 10 # 5-20, Medellín ", " ");

        assertThat(dto.nombre()).isEqualTo("Ana Ruiz");
        assertThat(dto.telefono()).isEqualTo("3001234567");
        assertThat(dto.direccion()).isEqualTo("Calle 10 # 5-20, Medellín");
        assertThat(dto.email()).isNull();
    }

    @Test
    void registrarFallaSiElTelefonoYaExiste() {
        when(clientes.existsByTelefono("3001234567")).thenReturn(true);

        assertThatThrownBy(() -> servicio.registrar("Ana Ruiz", "3001234567", "Calle 10", null))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("Ya existe un cliente");
        verify(clientes, never()).save(any());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "12345", "30012345678", "300abc4567", "+573001234567"})
    void registrarFallaConTelefonoInvalido(String telefono) {
        assertThatThrownBy(() -> servicio.registrar("Ana Ruiz", telefono, "Calle 10", null))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("no es válido");
        verify(clientes, never()).save(any());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void registrarFallaSinNombreOSinDireccion(String vacio) {
        assertThatThrownBy(() -> servicio.registrar(vacio, "3001234567", "Calle 10", null))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("nombre es obligatorio");
        assertThatThrownBy(() -> servicio.registrar("Ana Ruiz", "3001234567", vacio, null))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("dirección es obligatorio");
    }

    @Test
    void registrarFallaConCorreoMalFormado() {
        assertThatThrownBy(() -> servicio.registrar("Ana Ruiz", "3001234567", "Calle 10", "ana.ruiz"))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("formato válido");
    }

    @Test
    void buscarDevuelveElClienteDeEseTelefono() {
        when(clientes.findByTelefono("3001234567"))
                .thenReturn(Optional.of(new Cliente("Laura Gómez", "3001234567", "Calle 50", null)));

        assertThat(servicio.buscarPorTelefono("300 123 4567").nombre()).isEqualTo("Laura Gómez");
    }

    @Test
    void buscarAvisaSiElTelefonoNoEstaRegistrado() {
        when(clientes.findByTelefono("3000000000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.buscarPorTelefono("3000000000"))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("registrarCliente");
    }
}
