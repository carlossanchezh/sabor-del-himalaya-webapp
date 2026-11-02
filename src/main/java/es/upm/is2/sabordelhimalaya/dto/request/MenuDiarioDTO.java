package es.upm.is2.sabordelhimalaya.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Data
public class MenuDiarioDTO {

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) // el formulario envía la fecha como "2026-10-22"
    private LocalDate fecha;

    @NotNull
    private List<Long> idsEntrantes; // lista de entrantes (3 id entrante)

    @NotNull
    private List<Long> idsPrincipales; // lista de principales (3 id principal)

    @NotNull
    private List<Long> idsPostres; // lista de postres (2 id postre)

}
