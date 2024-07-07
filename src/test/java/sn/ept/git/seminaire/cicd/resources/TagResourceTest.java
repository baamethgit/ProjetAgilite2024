package sn.ept.git.seminaire.cicd.resources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import sn.ept.git.seminaire.cicd.ReplaceCamelCase;
import sn.ept.git.seminaire.cicd.exceptions.ItemNotFoundException;
import sn.ept.git.seminaire.cicd.models.TagDTO;
import sn.ept.git.seminaire.cicd.services.ITagService;
import sn.ept.git.seminaire.cicd.utils.TestUtil;
import sn.ept.git.seminaire.cicd.utils.UrlMapping;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TagResource.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
class TagResourceTest {

    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    private ITagService service;

    private TagDTO tag;

    @BeforeEach
    void setUp() {
        tag = new TagDTO();
        tag.setId(UUID.randomUUID().toString());
        tag.setName("Test Tag");
    }

    @Test
    void findAll_shouldReturnTags() throws Exception {
        List<TagDTO> list = List.of(tag);
        Mockito.when(service.findAll(Mockito.any(Pageable.class)))
                .thenReturn(new PageImpl<>(list));
        int size = list.size();

        mockMvc.perform(get(UrlMapping.Tag.ALL)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(size)))
                .andExpect(jsonPath("$.content.[0].id").exists())
                .andExpect(jsonPath("$.content.[0].name", is(tag.getName())));
    }

    @Test
    void findById_shouldReturnTag() throws Exception {
        Mockito.when(service.findById(Mockito.anyString()))
                .thenReturn(Optional.ofNullable(tag));

        mockMvc.perform(get(UrlMapping.Tag.FIND_BY_ID, tag.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name", is(tag.getName())));
    }

    @Test
    void findById_withBadId_shouldReturnNotFound() throws Exception {
        Mockito.when(service.findById(Mockito.anyString()))
                .thenReturn(Optional.empty());

        mockMvc.perform(get(UrlMapping.Tag.FIND_BY_ID, UUID.randomUUID().toString())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_shouldCreateTag() throws Exception {
        Mockito.when(service.save(Mockito.any()))
                .thenReturn(tag);

        mockMvc.perform(post(UrlMapping.Tag.ADD)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(tag)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name", is(tag.getName())));
    }

    @Test
    void update_shouldUpdateTag() throws Exception {
        Mockito.when(service.update(Mockito.anyString(), Mockito.any()))
                .thenReturn(tag);

        mockMvc.perform(put(UrlMapping.Tag.UPDATE, tag.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(tag)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name", is(tag.getName())));
    }

    @Test
    void delete_shouldDeleteTag() throws Exception {
        Mockito.doNothing().when(service).delete(Mockito.anyString());

        mockMvc.perform(delete(UrlMapping.Tag.DELETE, tag.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_withBadId_shouldReturnNotFound() throws Exception {
        Mockito.doThrow(new ItemNotFoundException()).when(service).delete(Mockito.anyString());

        mockMvc.perform(delete(UrlMapping.Tag.DELETE, UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
