package sn.ept.git.seminaire.cicd.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import sn.ept.git.seminaire.cicd.ReplaceCamelCase;
import sn.ept.git.seminaire.cicd.entities.Tag;
import sn.ept.git.seminaire.cicd.exceptions.ItemExistsException;
import sn.ept.git.seminaire.cicd.exceptions.ItemNotFoundException;
import sn.ept.git.seminaire.cicd.mappers.TagMapper;
import sn.ept.git.seminaire.cicd.models.TagDTO;
import sn.ept.git.seminaire.cicd.repositories.TagRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@DisplayNameGeneration(ReplaceCamelCase.class)
class TagServiceImplTest {

    @MockBean
    private TagRepository repository;

    @MockBean
    private TagMapper mapper;

    @Autowired
    private TagServiceImpl service;

    private Tag tag;
    private TagDTO tagDTO;

    @BeforeEach
    void setUp() {
        tag = new Tag();
        tag.setId(UUID.randomUUID().toString());
        tag.setName("Test Tag");
        tag.setDescription("Test Description");

        tagDTO = new TagDTO();
        tagDTO.setId(tag.getId());
        tagDTO.setName(tag.getName());
        tagDTO.setDescription(tag.getDescription());
    }

    @Test
    void saveTag_shouldSaveTag_whenValidInput() {
        when(repository.findByName(tagDTO.getName())).thenReturn(Optional.empty());
        when(mapper.toEntity(tagDTO)).thenReturn(tag);
        when(repository.saveAndFlush(tag)).thenReturn(tag);
        when(mapper.toDTO(tag)).thenReturn(tagDTO);

        TagDTO savedTag = service.save(tagDTO);

        assertNotNull(savedTag);
        assertEquals(tagDTO.getName(), savedTag.getName());
        verify(repository, times(1)).findByName(tagDTO.getName());
        verify(repository, times(1)).saveAndFlush(tag);
    }

    @Test
    void saveTag_shouldThrowException_whenTagNameExists() {
        when(repository.findByName(tagDTO.getName())).thenReturn(Optional.of(tag));

        assertThrows(ItemExistsException.class, () -> service.save(tagDTO));
        verify(repository, times(1)).findByName(tagDTO.getName());
        verify(repository, times(0)).saveAndFlush(any());
    }

    @Test
    void deleteTag_shouldDeleteTag_whenTagExists() {
        when(repository.findById(tag.getId())).thenReturn(Optional.of(tag));

        service.delete(tag.getId());

        verify(repository, times(1)).findById(tag.getId());
        verify(repository, times(1)).deleteById(tag.getId());
    }

    @Test
    void deleteTag_shouldThrowException_whenTagNotFound() {
        when(repository.findById(tag.getId())).thenReturn(Optional.empty());
        String uuid = tag.getId();
        assertThrows(ItemNotFoundException.class, () -> service.delete(uuid));
        verify(repository, times(1)).findById(tag.getId());
        verify(repository, times(0)).deleteById(any());
    }

    @Test
    void findById_shouldReturnTag_whenTagExists() {
        when(repository.findById(tag.getId())).thenReturn(Optional.of(tag));
        when(mapper.toDTO(tag)).thenReturn(tagDTO);

        Optional<TagDTO> foundTag = service.findById(tag.getId());

        assertTrue(foundTag.isPresent());
        assertEquals(tagDTO.getName(), foundTag.get().getName());
        verify(repository, times(1)).findById(tag.getId());
    }

    @Test
    void findById_shouldReturnEmpty_whenTagNotFound() {
        when(repository.findById(tag.getId())).thenReturn(Optional.empty());

        Optional<TagDTO> foundTag = service.findById(tag.getId());

        assertFalse(foundTag.isPresent());
        verify(repository, times(1)).findById(tag.getId());
    }

    @Test
    void findAll_shouldReturnListOfTags() {
        List<Tag> tags = List.of(tag);
        when(repository.findAll()).thenReturn(tags);
        when(mapper.toDTO(tag)).thenReturn(tagDTO);

        List<TagDTO> tagDTOList = service.findAll();

        assertNotNull(tagDTOList);
        assertEquals(1, tagDTOList.size());
        verify(repository, times(1)).findAll();
    }

    @Test
    void findAllPageable_shouldReturnPageOfTags() {
        PageRequest pageable = PageRequest.of(0, 10);
        List<Tag> tags = List.of(tag);
        PageImpl<Tag> tagPage = new PageImpl<>(tags, pageable, tags.size());
        when(repository.findAll(pageable)).thenReturn(tagPage);
        when(mapper.toDTO(tag)).thenReturn(tagDTO);

        Page<TagDTO> tagDTOPage = service.findAll(pageable);

        assertNotNull(tagDTOPage);
        assertEquals(1, tagDTOPage.getTotalElements());
        verify(repository, times(1)).findAll(pageable);
    }

    @Test
    void updateTag_shouldUpdateTag_whenValidInput() {
        when(repository.findById(tag.getId())).thenReturn(Optional.of(tag));
        when(repository.findByNameWithIdNotEquals(tagDTO.getName(), tag.getId())).thenReturn(Optional.empty());
        when(repository.saveAndFlush(tag)).thenReturn(tag);
        when(mapper.toDTO(tag)).thenReturn(tagDTO);

        TagDTO updatedTag = service.update(tag.getId(), tagDTO);

        assertNotNull(updatedTag);
        assertEquals(tagDTO.getName(), updatedTag.getName());
        verify(repository, times(1)).findById(tag.getId());
        verify(repository, times(1)).saveAndFlush(tag);
    }

    @Test
    void updateTag_shouldThrowException_whenTagNotFound() {
        when(repository.findById(tag.getId())).thenReturn(Optional.empty());
        String uuid = tag.getId();
        assertThrows(ItemNotFoundException.class, () -> service.update(uuid, tagDTO));
        verify(repository, times(1)).findById(tag.getId());
        verify(repository, times(0)).saveAndFlush(any());
    }

    @Test
    void updateTag_shouldThrowException_whenTagNameExists() {
        when(repository.findById(tag.getId())).thenReturn(Optional.of(tag));
        when(repository.findByNameWithIdNotEquals(tagDTO.getName(), tag.getId())).thenReturn(Optional.of(tag));
        String uuid = tag.getId();
        assertThrows(ItemExistsException.class, () -> service.update(uuid, tagDTO));
        verify(repository, times(1)).findById(tag.getId());
        verify(repository, times(1)).findByNameWithIdNotEquals(tagDTO.getName(), tag.getId());
        verify(repository, times(0)).saveAndFlush(any());
    }

    @Test
    void deleteAll_shouldDeleteAllTags() {
        service.deleteAll();
        verify(repository, times(1)).deleteAll();
    }
}
