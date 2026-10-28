package it.nexus.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import it.nexus.domain.BoardPost;
import it.nexus.domain.dto.BoardPostDTO;

@Mapper(uses = {ReferenceMapper.class, JobSlotMapper.class}, imports = TsidMapper.class)
public interface BoardPostMapper {

    @Mapping(target = "id", expression = "java(TsidMapper.toExternal(post.getId()))")
    @Mapping(target = "createdFromTicketId",
            expression = "java(post.getCreatedFromTicket() == null ? null : TsidMapper.toExternal(post.getCreatedFromTicket().getId()))")
    @Mapping(target = "createdFromTicketNumber", source = "createdFromTicket.number")
    BoardPostDTO toDto(BoardPost post);
}
