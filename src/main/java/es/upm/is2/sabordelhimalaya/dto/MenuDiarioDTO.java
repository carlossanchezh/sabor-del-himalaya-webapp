package es.upm.is2.sabordelhimalaya.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class MenuDiarioDTO {

    @NotNull
    private LocalDate fecha;

    @NotNull
    private List<Long> idsEntrantes; // lista de entrantes (3 id entrante)

    @NotNull
    private List<Long> idsPrincipales; // lista de principales (3 id principal)

    @NotNull
    private List<Long> idsPostres; // lista de postres (2 id postre)

}
