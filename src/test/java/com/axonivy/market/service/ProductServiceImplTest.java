package com.axonivy.market.service;

import static com.axonivy.market.constants.CommonConstants.LOGO_FILE;
import static com.axonivy.market.constants.CommonConstants.META_FILE;
import static com.axonivy.market.constants.CommonConstants.SLASH;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import com.axonivy.market.entity.GithubRepoMeta;
import com.axonivy.market.github.service.GHAxonIvyProductRepoService;
import com.axonivy.market.github.service.GithubService;
import com.axonivy.market.model.ReadmeModel;
import com.axonivy.market.repository.GithubRepoMetaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.entity.Product;
import com.axonivy.market.enums.FileStatus;
import com.axonivy.market.enums.FileType;
import com.axonivy.market.enums.FilterType;
import com.axonivy.market.enums.SortOption;
import com.axonivy.market.github.model.GitHubFile;
import com.axonivy.market.github.service.GHAxonIvyMarketRepoService;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.impl.ProductServiceImpl;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    private static final String SAMPLE_PRODUCT_NAME = "amazon-comprehend";
    private static final long LAST_CHANGE_TIME = 1718096290000l;
    private static final Pageable PAGEABLE = PageRequest.of(0, 20,
            Sort.by(SortOption.ALPHABETICALLY.getOption()).descending());
    private static final String SHA1_SAMPLE = "35baa89091b2452b77705da227f1a964ecabc6c8";
    private String keyword;
    private Page<Product> mockResultReturn;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private GHAxonIvyMarketRepoService marketRepoService;

    @Mock
    private GHAxonIvyProductRepoService productRepoService;

    @Mock
    private GithubRepoMetaRepository repoMetaRepository;

    @Mock
    private GithubService githubService;

    @Mock
    private GHRepository mockRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @BeforeEach
    public void setup() {
        mockResultReturn = createPageProductsMock();
    }

    @Test
    void testFindProducts() throws IOException {
        // Start testing by All
        when(productRepository.findAll(any(Pageable.class))).thenReturn(mockResultReturn);
        mockMarketRepoMetaStatus();
        // Executes
        var result = productService.findProducts(FilterType.ALL.getOption(), keyword, PAGEABLE);
        assertEquals(mockResultReturn, result);

        // Start testing by Other
        // Executes
        result = productService.findProducts(FilterType.DEMOS.getOption(), keyword, PAGEABLE);
        assertEquals(0, result.getSize());
    }

    @Test
    void testFindProductsAsUpdateMetaJSONFromGitHub() throws IOException {
        // Start testing by adding new meta
        mockMarketRepoMetaStatus();
        var mockCommit = mockGHCommitHasSHA1(UUID.randomUUID().toString());
        when(mockCommit.getCommitDate()).thenReturn(new Date());
        when(marketRepoService.getLastCommit(anyLong())).thenReturn(mockCommit);

        var connectorCode = FilterType.CONNECTORS.getCode();
        var connectorProducts = mockResultReturn.filter(product -> product.getType().equals(connectorCode)).toList();
        var mockPagedResult = new PageImpl<Product>(connectorProducts);
        when(productRepository.findByType(connectorCode, PAGEABLE)).thenReturn(mockPagedResult);

        var mockGithubFile = new GitHubFile();
        mockGithubFile.setFileName(META_FILE);
        mockGithubFile.setType(FileType.META);
        mockGithubFile.setStatus(FileStatus.ADDED);
        when(marketRepoService.fetchMarketItemsBySHA1Range(any(), any())).thenReturn(List.of(mockGithubFile));
        var mockGHContent = mockGHContentAsMetaJSON();
        when(githubService.getGHContent(any(), anyString())).thenReturn(mockGHContent);

        // Executes
        var result = productService.findProducts(FilterType.CONNECTORS.getOption(), keyword, PAGEABLE);
        assertEquals(mockPagedResult, result);

        // Start testing by deleting new meta
        mockCommit = mockGHCommitHasSHA1(UUID.randomUUID().toString());
        when(mockCommit.getCommitDate()).thenReturn(new Date());
        when(marketRepoService.getLastCommit(anyLong())).thenReturn(mockCommit);
        mockGithubFile.setStatus(FileStatus.REMOVED);
        // Executes
        result = productService.findProducts(FilterType.CONNECTORS.getOption(), keyword, PAGEABLE);
        assertEquals(mockPagedResult, result);
    }

    @Test
    void testFindProductsAsUpdateLogoFromGitHub() throws IOException {
        // Start testing by adding new logo
        mockMarketRepoMetaStatus();
        var mockCommit = mockGHCommitHasSHA1(UUID.randomUUID().toString());
        when(mockCommit.getCommitDate()).thenReturn(new Date());
        when(marketRepoService.getLastCommit(anyLong())).thenReturn(mockCommit);

        var connectorCode = FilterType.CONNECTORS.getCode();
        var connectorProducts = mockResultReturn.filter(product -> product.getType().equals(connectorCode)).toList();
        var mockPagedResult = new PageImpl<Product>(connectorProducts);
        when(productRepository.findByType(connectorCode, PAGEABLE)).thenReturn(mockPagedResult);
        var mockGithubFile = mock(GitHubFile.class);
        mockGithubFile = new GitHubFile();
        mockGithubFile.setFileName(LOGO_FILE);
        mockGithubFile.setType(FileType.LOGO);
        mockGithubFile.setStatus(FileStatus.ADDED);
        when(marketRepoService.fetchMarketItemsBySHA1Range(any(), any())).thenReturn(List.of(mockGithubFile));
        var mockGHContent = mockGHContentAsMetaJSON();
        when(githubService.getGHContent(any(), anyString())).thenReturn(mockGHContent);

        // Executes
        var result = productService.findProducts(FilterType.CONNECTORS.getOption(), keyword, PAGEABLE);
        assertEquals(mockPagedResult, result);

        // Start testing by deleting new logo
        when(mockCommit.getSHA1()).thenReturn(UUID.randomUUID().toString());
        mockGithubFile.setStatus(FileStatus.REMOVED);
        when(marketRepoService.fetchMarketItemsBySHA1Range(any(), any())).thenReturn(List.of(mockGithubFile));
        when(githubService.getGHContent(any(), anyString())).thenReturn(mockGHContent);
        when(productRepository.findByLogoUrl(any())).thenReturn(new Product());

        // Executes
        result = productService.findProducts(FilterType.CONNECTORS.getOption(), keyword, PAGEABLE);
        assertEquals(mockPagedResult, result);
    }

    //TODO
    @Test
    void testFindAllProductsFirstTime() throws IOException {
        when(githubService.getRepository(any())).thenReturn(mockRepository);
        String allType = FilterType.ALL.getOption();
        when(productRepository.findAll(any(Pageable.class))).thenReturn(mockResultReturn);

        var mockCommit = mockGHCommitHasSHA1(SHA1_SAMPLE);
        when(marketRepoService.getLastCommit(anyLong())).thenReturn(mockCommit);
        when(repoMetaRepository.findByRepoName(anyString())).thenReturn(null);

        var mockContent = mockGHContentAsMetaJSON();
        InputStream inputStream = this.getClass().getResourceAsStream(SLASH.concat(META_FILE));
        when(mockContent.read()).thenReturn(inputStream);

        Map<String, List<GHContent>> mockGHContentMap = new HashMap<>();
        mockGHContentMap.put(SAMPLE_PRODUCT_NAME, List.of(mockContent));
        when(marketRepoService.fetchAllMarketItems()).thenReturn(mockGHContentMap);

        var result = productService.findProducts(allType, keyword, PAGEABLE);
        assertEquals(result, mockResultReturn);
        verify(productRepository).findAll(any(Pageable.class));
        verify(productRepository).saveAll(anyList());

    }
    @Test
    void testFindAllProductsFirstTime21() throws IOException {
        // Mocking the repository to return a mock repository object
        GHRepository mockRepository = mock(GHRepository.class);
        when(githubService.getRepository(any())).thenReturn(mockRepository);

        // Setting up the filter type and pageable return value
        String allType = FilterType.ALL.getOption();
        when(productRepository.findAll(any(Pageable.class))).thenReturn(mockResultReturn);

        // Mocking the last commit with a sample SHA1
        var mockCommit = mockGHCommitHasSHA1(SHA1_SAMPLE);
        when(marketRepoService.getLastCommit(anyLong())).thenReturn(mockCommit);

        // Returning null for the repository meta data to simulate the first-time sync
        when(repoMetaRepository.findByRepoName(anyString())).thenReturn(null);

        // Mocking GHContent as meta JSON
        var mockContent = mockGHContentAsMetaJSON();
        InputStream inputStream = this.getClass().getResourceAsStream(SLASH.concat(META_FILE));
        when(mockContent.read()).thenReturn(inputStream);

        // Creating a map of mock GHContent and setting up the market repo service
        Map<String, List<GHContent>> mockGHContentMap = new HashMap<>();
        mockGHContentMap.put(SAMPLE_PRODUCT_NAME, List.of(mockContent));
        when(marketRepoService.fetchAllMarketItems()).thenReturn(mockGHContentMap);

        // Mocking the tags for the repository
        PagedIterable<GHTag> mockTags = mock(PagedIterable.class);
        GHTag mockOldestTag = mock(GHTag.class);
        GHCommit mockCommitForTag = mock(GHCommit.class);
        when(mockOldestTag.getCommit()).thenReturn(mockCommitForTag);
        when(mockCommitForTag.getCommitDate()).thenReturn(new Date());
        when(mockOldestTag.getName()).thenReturn("v1.0.0");
        when(mockTags.toList()).thenReturn(List.of(mockOldestTag));
        when(mockRepository.listTags()).thenReturn(mockTags);

        // Executing the findProducts method and verifying the results
        var result = productService.findProducts(allType, keyword, PAGEABLE);
        assertEquals(result, mockResultReturn);
        verify(productRepository).findAll(any(Pageable.class));
        verify(productRepository).saveAll(anyList());

        // Verifying the compatibility extraction logic
        verify(productService).extractCompatibilityFromOldestTag(any(Product.class));
    }

    @Test
    void testFindAllProductsFirstTime11() throws IOException {
        when(githubService.getRepository(any())).thenReturn(mockRepository);
        String allType = FilterType.ALL.getOption();
        when(productRepository.findAll(any(Pageable.class))).thenReturn(mockResultReturn);

        var mockCommit = mockGHCommitHasSHA1(SHA1_SAMPLE);
        when(marketRepoService.getLastCommit(anyLong())).thenReturn(mockCommit);
        when(repoMetaRepository.findByRepoName(anyString())).thenReturn(null);

        var mockContent = mockGHContentAsMetaJSON();
        InputStream inputStream = this.getClass().getResourceAsStream(SLASH.concat(META_FILE));
        when(mockContent.read()).thenReturn(inputStream);

        Map<String, List<GHContent>> mockGHContentMap = new HashMap<>();
        mockGHContentMap.put(SAMPLE_PRODUCT_NAME, List.of(mockContent));
        when(marketRepoService.fetchAllMarketItems()).thenReturn(mockGHContentMap);

        // Mock tags for the repository
        GHTag newestTag = mock(GHTag.class);
        GHCommit newestCommit = mockCommitWithDate(LocalDate.of(2023, 1, 1));
        when(newestTag.getName()).thenReturn("v2.0");
        when(newestTag.getCommit()).thenReturn(newestCommit);

        GHTag oldestTag = mock(GHTag.class);
        GHCommit oldestCommit = mockCommitWithDate(LocalDate.of(2021, 1, 1));
        when(oldestTag.getName()).thenReturn("v1.0");
        when(oldestTag.getCommit()).thenReturn(oldestCommit);

        List<GHTag> tagList = Arrays.asList(oldestTag, newestTag);
        PagedIterable<GHTag> pagedIterableTags = mockPagedIterable(tagList);
        when(mockRepository.listTags()).thenReturn(pagedIterableTags);

        var result = productService.findProducts(allType, keyword, PAGEABLE);
        assertEquals(result, mockResultReturn);
        verify(productRepository).findAll(any(Pageable.class));
        verify(productRepository).saveAll(anyList());

        // Verify the compatibility extraction
        for (Product product : result.getContent()) {
            verify(product).setCompatibility("1.0+");
        }
    }

    // Helper method to mock PagedIterable
    private <T> PagedIterable<T> mockPagedIterable(List<T> elements) {
        PagedIterable<T> pagedIterable = mock(PagedIterable.class);
        PagedIterator<T> pagedIterator = mock(PagedIterator.class);
        Iterator<T> iterator = elements.iterator();
        when(pagedIterator.hasNext()).thenAnswer(invocation -> iterator.hasNext());
        when(pagedIterator.next()).thenAnswer(invocation -> iterator.next());
        when(pagedIterable.iterator()).thenReturn(pagedIterator);
        return pagedIterable;
    }

    // Helper method to mock GHCommit with a specific date
    private GHCommit mockCommitWithDate(LocalDate date) throws IOException {
        GHCommit commit = mock(GHCommit.class);
        GHCommit.ShortInfo commitShortInfo = mock(GHCommit.ShortInfo.class);
        Date commitDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        when(commit.getCommitDate()).thenReturn(commitDate);
        when(commit.getCommitShortInfo()).thenReturn(commitShortInfo);
        return commit;
    }


    @Test
    void testFindAllProductsFirstTime1() throws IOException {
        // Mocking necessary dependencies
        when(githubService.getRepository(any())).thenReturn(mockRepository);
        String allType = FilterType.ALL.getOption();
        when(productRepository.findAll(any(Pageable.class))).thenReturn(mockResultReturn);

        var mockCommit = mockGHCommitHasSHA1(SHA1_SAMPLE);
        when(marketRepoService.getLastCommit(anyLong())).thenReturn(mockCommit);
        when(repoMetaRepository.findByRepoName(anyString())).thenReturn(null);

        var mockContent = mockGHContentAsMetaJSON();
        InputStream inputStream = getClass().getResourceAsStream("/meta.json"); // Assuming META_FILE is defined somewhere as META_FILE = "meta.json"
        when(mockContent.read()).thenReturn(inputStream);

        Map<String, List<GHContent>> mockGHContentMap = new HashMap<>();
        mockGHContentMap.put(SAMPLE_PRODUCT_NAME, List.of(mockContent));
        when(marketRepoService.fetchAllMarketItems()).thenReturn(mockGHContentMap);

        // Mocking scenario where listTags() returns an empty list
        PagedIterable<GHTag> mockTags = mock(PagedIterable.class);
        when(mockTags.toList()).thenReturn(Collections.emptyList());
        GHRepository mockRepository = mock(GHRepository.class);
        when(mockRepository.listTags()).thenReturn(mockTags);
        when(githubService.getRepository(any())).thenReturn(mockRepository);

        // Calling the method under test
        var result = productService.findProducts(allType, keyword, PAGEABLE);

        // Assertions and verifications
        assertEquals(mockResultReturn, result);
        verify(productRepository).findAll(any(Pageable.class));
        verify(productRepository).saveAll(anyList());
    }

    @Test
    void testFindAllProducts2FirstTime() throws IOException {
        // Mocking necessary dependencies
        when(githubService.getRepository(any())).thenReturn(mockRepository);
        String allType = FilterType.ALL.getOption();
        when(productRepository.findAll(any(Pageable.class))).thenReturn(mockResultReturn);

        // Mocking GHRepository and PagedIterable
        GHRepository mockRepository = mock(GHRepository.class);
        PagedIterable<GHTag> mockTags = mock(PagedIterable.class);
        when(mockRepository.listTags()).thenReturn(mockTags);
        when(mockTags.toList()).thenReturn(Collections.emptyList()); // Mocking an empty list of tags

        // Mocking other dependencies
        when(marketRepoService.getLastCommit(anyLong())).thenReturn(mockGHCommitHasSHA1(SHA1_SAMPLE));
        when(repoMetaRepository.findByRepoName(anyString())).thenReturn(null);

        var mockContent = mockGHContentAsMetaJSON();
        InputStream inputStream = getClass().getResourceAsStream("/meta.json"); // Assuming META_FILE is defined somewhere as META_FILE = "meta.json"
        when(mockContent.read()).thenReturn(inputStream);

        Map<String, List<GHContent>> mockGHContentMap = new HashMap<>();
        mockGHContentMap.put(SAMPLE_PRODUCT_NAME, List.of(mockContent));
        when(marketRepoService.fetchAllMarketItems()).thenReturn(mockGHContentMap);

        // Calling the method under test
        var result = productService.findProducts(allType, keyword, PAGEABLE);

        // Assertions and verifications
        assertEquals(mockResultReturn, result);
        verify(productRepository).findAll(any(Pageable.class));
        verify(productRepository).saveAll(anyList());
    }



    @Test
    void testSearchProducts() {
        var simplePageable = PageRequest.of(0, 20);
        String type = FilterType.ALL.getOption();
        keyword = "on";
        when(productRepository.searchByNameOrShortDescriptionRegex(keyword, simplePageable)).thenReturn(mockResultReturn);

        var result = productService.findProducts(type, keyword, simplePageable);
        assertEquals(result, mockResultReturn);
        verify(productRepository).searchByNameOrShortDescriptionRegex(keyword, simplePageable);
    }

    @Test
    void testExtractCompatibilityFromOldestTag_shouldNotChangeCompatibilityIfAlreadySet() {
        Product product = new Product();
        product.setCompatibility("1.0+");
        productService.extractCompatibilityFromOldestTag(product);
        assertEquals("1.0+", product.getCompatibility());
    }

    @Test
    void testExtractCompatibilityFromOldestTag_shouldSetCompatibilityBasedOnNoDotsOldestTag() throws IOException {
        Product product = new Product();
        product.setRepositoryName("Docker");

        GHRepository mockRepository = mock(GHRepository.class);
        GHTag oldestTag = mock(GHTag.class);
        when(oldestTag.getName()).thenReturn("v8");

        PagedIterable<GHTag> pagedIterable = mock(PagedIterable.class);
        when(pagedIterable.toList()).thenReturn(List.of(oldestTag));
        when(mockRepository.listTags()).thenReturn(pagedIterable);
        when(githubService.getRepository(anyString())).thenReturn(mockRepository);

        productService.extractCompatibilityFromOldestTag(product);

        assertEquals("8.0+", product.getCompatibility());
    }

    @Test
    void testExtractCompatibilityFromOldestTag_shouldSetCompatibilityBasedOnOneDotOldestTag() throws IOException {
        Product product = new Product();
        product.setRepositoryName("Docker");

        GHRepository mockRepository = mock(GHRepository.class);
        GHTag oldestTag = mock(GHTag.class);
        when(oldestTag.getName()).thenReturn("release_11.0-SNAPSHOT");

        PagedIterable<GHTag> pagedIterable = mock(PagedIterable.class);
        when(pagedIterable.toList()).thenReturn(List.of(oldestTag));
        when(mockRepository.listTags()).thenReturn(pagedIterable);
        when(githubService.getRepository(anyString())).thenReturn(mockRepository);

        productService.extractCompatibilityFromOldestTag(product);

        assertEquals("11.0+", product.getCompatibility());
    }
    @Test
    void testExtractCompatibilityFromOldestTag_shouldSetCompatibilityBasedOnMoreThanOneDotOldestTag() throws IOException {
        Product product = new Product();
        product.setRepositoryName("Basic Workflow UI");

        GHRepository mockRepository = mock(GHRepository.class);
        GHTag oldestTag = mock(GHTag.class);
        when(oldestTag.getName()).thenReturn("s9.2.0.S18");

        PagedIterable<GHTag> pagedIterable = mock(PagedIterable.class);
        when(pagedIterable.toList()).thenReturn(List.of(oldestTag));
        when(mockRepository.listTags()).thenReturn(pagedIterable);
        when(githubService.getRepository(anyString())).thenReturn(mockRepository);

        productService.extractCompatibilityFromOldestTag(product);
        assertEquals("9.2+", product.getCompatibility());
    }

    @Test
    void testFetchProductDetail() {
        String id = "amazon-comprehend";
        String type = "connector";
        Product mockProduct = mockResultReturn.getContent().get(0);
        when(productRepository.findByIdAndType(id, type)).thenReturn(mockProduct);
        Product result = productService.fetchProductDetail(id, type);
        assertEquals(mockProduct, result);
        verify(productRepository, times(1)).findByIdAndType(id, type);
    }
    @Test
    void testGetReadmeAndProductContentsFromTag_success() {
        // Arrange
        String productId = "amazon-comprehend";
        String tag = "v1.0";
        String repoName = "testRepo";

        Product product = new Product();
        product.setRepositoryName(repoName);

        ReadmeModel expectedReadmeModel = new ReadmeModel();
        // Set the expected values for the ReadmeModel as per your logic

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
//        when(productRepoService.getReadmeAndProductContentsFromTag(repoName, tag)).thenReturn(expectedReadmeModel);

        // Act
        ReadmeModel result = productRepoService.getReadmeAndProductContentsFromTag(repoName, tag);

        // Assert
        assertEquals(expectedReadmeModel, result);
    }

    @Test
    void testGetReadmeAndProductContentsFromTag_productNotFound() {
        // Arrange
        String productId = "1";
        String tag = "v1.0";

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act
        ReadmeModel result = productRepoService.getReadmeAndProductContentsFromTag(productId, tag);

        // Assert
        assertNull(result);
    }

    @Test
    void testGetReadmeAndProductContentsFromTag_exception() {
        // Arrange
        String productId = "1";
        String tag = "v1.0";
        String repoName = "testRepo";

        Product product = new Product();
        product.setRepositoryName(repoName);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepoService.getReadmeAndProductContentsFromTag(repoName, tag)).thenThrow(new RuntimeException("Exception"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            productRepoService.getReadmeAndProductContentsFromTag(productId, tag);
        });
    }
    //TODO
    @Test
    void testGetReadmeAndProductContentsFromTag() {
//        Page<Product> mockProductsPage = createPageProductsMock();
//        Product mockProduct = mockProductsPage.getContent().get(0);
//
//        String productId = "amazon-comprehend";
//        String tag = "v1.0";
//        ReadmeModel mockReadmeModel = mockReadme();
////    when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));
//        when(productService.getReadmeAndProductContentsFromTag(mockProduct.getRepositoryName(), tag)).thenReturn(mockReadmeModel);
//
//        ReadmeModel result = productService.getReadmeAndProductContentsFromTag(productId, tag);
//
//        assertEquals(mockReadmeModel, result);
//        verify(productRepository, times(1)).findById(productId);
//        verify(productService, times(1)).getReadmeAndProductContentsFromTag(mockProduct.getRepositoryName(), tag);
    }

    private Page<Product> createPageProductsMock() {
        var mockProducts = new ArrayList<Product>();
        Product mockProduct = new Product();
        mockProduct.setId(SAMPLE_PRODUCT_NAME);
        mockProduct.setName("Amazon Comprehend");
        mockProduct.setType("connector");
        mockProduct.setCompatibility("1.0+");
        mockProducts.add(mockProduct);

        mockProduct = new Product();
        mockProduct.setId("tel-search-ch-connector");
        mockProduct.setName("Swiss phone directory");
        mockProduct.setType("util");
        mockProducts.add(mockProduct);
        return new PageImpl<>(mockProducts);
    }

    private void mockMarketRepoMetaStatus() {
        var mockMartketRepoMeta = new GithubRepoMeta();
        mockMartketRepoMeta.setRepoURL(GitHubConstants.AXONIVY_MARKETPLACE_REPO_NAME);
        mockMartketRepoMeta.setRepoName(GitHubConstants.AXONIVY_MARKETPLACE_REPO_NAME);
        mockMartketRepoMeta.setLastChange(LAST_CHANGE_TIME);
        mockMartketRepoMeta.setLastSHA1(SHA1_SAMPLE);
        when(repoMetaRepository.findByRepoName(any())).thenReturn(mockMartketRepoMeta);
    }

    private ReadmeModel mockReadme() {
        ReadmeModel mockReadmeModel = new ReadmeModel();
        mockReadmeModel.setTag("v1.0");
        mockReadmeModel.setDescription("Description content");
        mockReadmeModel.setDemo("Demo content");
        mockReadmeModel.setSetup("Setup content");
        return mockReadmeModel;
    }

    private GHCommit mockGHCommitHasSHA1(String sha1) {
        var mockCommit = mock(GHCommit.class);
        when(mockCommit.getSHA1()).thenReturn(sha1);
        return mockCommit;
    }

    private GHContent mockGHContentAsMetaJSON() {
        var mockGHContent = mock(GHContent.class);
        when(mockGHContent.getName()).thenReturn(META_FILE);
        return mockGHContent;
    }
}