package it.nexus.services;

import java.util.List;

import it.nexus.domain.dto.BoardPostCreateDTO;
import it.nexus.domain.dto.BoardPostDTO;
import it.nexus.domain.dto.BoardPostFormDTO;
import it.nexus.domain.enumeration.BoardPostStatus;

/** Gestione della bacheca per Call Center e ADMIN (US-902). */
public interface BoardPostService {

    List<BoardPostDTO> list(BoardPostStatus status);

    BoardPostDTO get(String id);

    /** Bozza da una mansione libera e a disposizione, senza altri annunci attivi (US-904). */
    BoardPostDTO create(BoardPostCreateDTO dto);

    BoardPostDTO update(String id, BoardPostFormDTO dto);

    BoardPostDTO publish(String id, long expectedVersion);

    BoardPostDTO archive(String id, long expectedVersion);
}
