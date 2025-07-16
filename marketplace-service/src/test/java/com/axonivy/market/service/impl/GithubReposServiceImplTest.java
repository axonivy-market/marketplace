package com.axonivy.market.service.impl;

import com.axonivy.market.assembler.GithubReposModelAssembler;
import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.entity.Product;
import com.axonivy.market.model.GithubReposModel;
import com.axonivy.market.repository.GithubRepoRepository;
import com.axonivy.market.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GithubReposServiceImplTest {

    @Mock
    private GithubRepoRepository githubRepoRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private GithubReposModelAssembler githubReposModelAssembler;

    @Mock
    private WorkflowServiceImpl workflowService;

    @InjectMocks
    private GithubReposServiceImpl githubReposService;

    @Test
    void loadAndStoreTestReportsSuccess() {
        Product product1 = new Product();
        Product product2 = new Product();
        List<Product> products = Arrays.asList(product1, product2);
        when(productRepository.findAll()).thenReturn(products);

        githubReposService.loadAndStoreTestReports();

        verify(productRepository).findAll();
      verify(workflowService, times(2)).processProduct(any(Product.class));
      verify(workflowService, times(2)).processProduct(any(Product.class));    }

    @Test
    void loadAndStoreTestReportsHandlesException() {
        when(productRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        githubReposService.loadAndStoreTestReports();


        verify(productRepository).findAll();
        verifyNoInteractions(workflowService);
    }

    @Test
    void fetchAllRepositoriesReturnsAllRepositoriesMappedToModels() {
        GithubRepo repo1 = new GithubRepo();
        GithubRepo repo2 = new GithubRepo();
        List<GithubRepo> repos = Arrays.asList(repo1, repo2);

        GithubReposModel model1 = new GithubReposModel();
        GithubReposModel model2 = new GithubReposModel();

        when(githubRepoRepository.findAll()).thenReturn(repos);
        when(githubReposModelAssembler.toModel(repo1)).thenReturn(model1);
        when(githubReposModelAssembler.toModel(repo2)).thenReturn(model2);

        List<GithubReposModel> result = githubReposService.fetchAllRepositories();

        assertEquals(2, result.size());
        assertEquals(model1, result.get(0));
        assertEquals(model2, result.get(1));
        verify(githubRepoRepository).findAll();
        verify(githubReposModelAssembler).toModel(repo1);
        verify(githubReposModelAssembler).toModel(repo2);
    }
}