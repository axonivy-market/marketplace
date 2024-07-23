import { ProductApiResponse } from '../models/apis/product-response.model';
import { ProductDetail } from '../models/product-detail.model';
import { ProductModuleContent } from '../models/product-module-content.model';

export const MOCK_PRODUCTS = {
  _embedded: {
    products: [
      {
        id: 'amazon-comprehend',
        names: {
          en: 'Amazon Comprehend',
          de: 'TODO Amazon Comprehend'
        },
        shortDescriptions: {
          en: 'Amazon Comprehend is a AI service that uses machine learning to uncover information in unstructured data.',
          de: 'Amazon Comprehend ist ein KI-Service, der maschinelles Lernen nutzt, um aus unstrukturierten Daten wertvolle Informationen zu generieren.'
        },
        logoUrl:
          'https://raw.githubusercontent.com/axonivy-market/market/master/market/connector/amazon-comprehend/logo.png',
        type: 'connector',
        tags: ['AI'],
        _links: {
          self: {
            href: 'http://localhost:8080/marketplace-service/api/product-details/amazon-comprehend?type=connector'
          }
        }
      }
    ]
  },
  _links: {
    first: {
      href: 'http://localhost:8080/marketplace-service/api/product?type=all&page=0&size=20'
    },
    self: {
      href: 'http://localhost:8080/marketplace-service/api/product?type=all&page=0&size=20'
    },
    next: {
      href: 'http://localhost:8080/marketplace-service/api/product?type=all&page=1&size=20'
    },
    last: {
      href: 'http://localhost:8080/marketplace-service/api/product?type=all&page=3&size=20'
    }
  },
  page: {
    size: 20,
    totalElements: 70,
    totalPages: 4,
    number: 0
  }
} as unknown as ProductApiResponse;

export const MOCK_EMPTY_DE_VALUES_AND_NO_LOGO_URL_PRODUCTS = {
  _embedded: {
    products: [
      {
        id: 'amazon-comprehend',
        names: {
          en: 'Amazon Comprehend',
          de: ''
        },
        shortDescriptions: {
          en: 'Amazon Comprehend is a AI service that uses machine learning to uncover information in unstructured data.',
          de: ''
        },
        logoUrl: '',
        type: 'connector',
        tags: ['AI'],
        _links: {
          self: {
            href: 'http://localhost:8080/marketplace-service/api/product-details/amazon-comprehend?type=connector'
          }
        }
      }
    ]
  },
  _links: {
    first: {
      href: 'http://localhost:8080/marketplace-service/api/product?type=all&page=0&size=20'
    },
    self: {
      href: 'http://localhost:8080/marketplace-service/api/product?type=all&page=0&size=20'
    },
    next: {
      href: 'http://localhost:8080/marketplace-service/api/product?type=all&page=1&size=20'
    },
    last: {
      href: 'http://localhost:8080/marketplace-service/api/product?type=all&page=3&size=20'
    }
  },
  page: {
    size: 20,
    totalElements: 70,
    totalPages: 4,
    number: 0
  }
} as unknown as ProductApiResponse;

export const MOCK_PRODUCTS_FILTER_CONNECTOR = {
  _embedded: {
    products: [
      {
        id: 'amazon-comprehend',
        names: {
          en: 'Amazon Comprehend',
          de: 'TODO Amazon Comprehend'
        },
        shortDescriptions: {
          en: 'Amazon Comprehend is a AI service that uses machine learning to uncover information in unstructured data.',
          de: 'Amazon Comprehend ist ein KI-Service, der maschinelles Lernen nutzt, um aus unstrukturierten Daten wertvolle Informationen zu generieren.'
        },
        logoUrl:
          'https://raw.githubusercontent.com/axonivy-market/market/master/market/connector/amazon-comprehend/logo.png',
        type: 'connector',
        tags: ['AI'],
        _links: {
          self: {
            href: 'http://localhost:8080/marketplace-service/api/product-details/amazon-comprehend?type=connector'
          }
        }
      },
      {
        id: 'a-trust',
        names: {
          en: 'A-Trust',
          de: 'A-Trust'
        },
        shortDescriptions: {
          en: 'Clearly authenticate your Austrian customers with a mobile phone signature.',
          de: 'Clearly authenticate your Austrian customers with a mobile phone signature.'
        },
        logoUrl:
          'https://raw.githubusercontent.com/axonivy-market/market/master/market/connector/a-trust/logo.png',
        type: 'connector',
        tags: ['e-signature'],
        _links: {
          self: {
            href: 'http://localhost:8080/marketplace-service/api/product-details/a-trust?type=connector'
          }
        }
      },
      {
        id: 'mailstore-connector',
        names: {
          en: 'Mailstore',
          de: 'Mailstore'
        },
        shortDescriptions: {
          en: 'Enhance business processes by streamlining email management, supporting both IMAP and POP3 with robust SSL encryption.',
          de: 'Enhance business processes by streamlining email management, supporting both IMAP and POP3 with robust SSL encryption.'
        },
        logoUrl:
          'https://raw.githubusercontent.com/axonivy-market/market/master/market/connector/mailstore-connector/logo.png',
        type: 'connector',
        tags: ['office', 'email'],
        _links: {
          self: {
            href: 'http://localhost:8080/marketplace-service/api/product-details/mailstore-connector?type=connector'
          }
        }
      }
    ]
  },
  _links: {
    first: {
      href: 'http://localhost:8080/marketplace-service/api/product?type=all&page=0&size=20'
    },
    self: {
      href: 'http://localhost:8080/marketplace-service/api/product?type=all&page=0&size=20'
    },
    next: {
      href: 'http://localhost:8080/marketplace-service/api/product?type=all&page=1&size=20'
    },
    last: {
      href: 'http://localhost:8080/marketplace-service/api/product?type=all&page=3&size=20'
    }
  },
  page: {
    size: 20,
    totalElements: 70,
    totalPages: 4,
    number: 0
  }
} as unknown as ProductApiResponse;

export const MOCK_PRODUCTS_NEXT_PAGE = {
  _embedded: {
    products: []
  },
  _links: {
    first: {
      href: 'http://localhost:8080/marketplace-service/api/product?type=all&page=0&size=20'
    },
    self: {
      href: 'http://localhost:8080/marketplace-service/api/product?type=all&page=1&size=20'
    }
  },
  page: {
    size: 20,
    totalElements: 1,
    totalPages: 1,
    number: 1
  }
} as ProductApiResponse;

export const MOCK_PRODUCT_MODULE_CONTENT: ProductModuleContent = {
  tag: 'v10.0.10',
  description: null,
  demo: '',
  setup: '',
  isDependency: false,
  name: 'Jira Connector',
  groupId: 'com.axonivy.connector.jira',
  artifactId: 'jira-connector',
  type: 'iar'
};

export const MOCK_PRODUCT_DETAILS: ProductDetail = {
  id: 'jira-connector',
  names: {
    en: 'Atlassian Jira',
    de: 'TODO Atlassian Jira'
  },
  shortDescriptions: {
    en: "Atlassian's Jira connector lets you track issues directly from the Axon Ivy platform.",
    de: "TODO Atlassian's Jira connector lets you track issues directly from the Axon Ivy platform."
  },
  installationCount: 1,
  logoUrl:
    'https://raw.githubusercontent.com/axonivy-market/market/master/market/connector/jira/logo.png',
  type: 'connector',
  tags: ['helper'],
  vendor: 'FROX AG',
  vendorUrl: 'https://www.frox.ch',
  platformReview: '4.5',
  newestReleaseVersion: 'v10.0.0',
  cost: 'Free',
  sourceUrl: 'https://github.com/axonivy-market/jira-connector',
  statusBadgeUrl:
    'https://github.com/axonivy-market/jira-connector/actions/workflows/ci.yml/badge.svg',
  language: 'English',
  industry: 'Cross-Industry',
  compatibility: '9.2+',
  contactUs: false,
  productModuleContent: {
    tag: 'v10.0.0',
    description: {
      en: "Axon Ivy's [Atlassian Jira Connector ](https://www.atlassian.com/software/jira) gives you full power to track issues within your process work. The connector:\n\n- Features three main functionalities (create comment, create issue, and get issue).\n- Provides access to the core API of Atlassian Jira.\n- Supports you with an easy-to-copy demo implementation to reduce your integration effort.\n- Enables low code citizen developers to integrate issue tracking tools without writing a single line of code."
    },
    setup:
      'Open the `Config/variables.yaml` in your Axon Ivy Designer and paste the\ncode below and adjust the values to your environment.\n\n```\nVariables:\n\n  jira-connector:\n  \n    # Url to the Jira server\n    Url: "https://localhost"\n\n    # Username to connect to the Jira server\n    Username: "admin"\n\n    # Password to connect to the Jira server\n    Password: "1234"\n```',
    demo: '![jira-connector Demo 1](https://raw.githubusercontent.com/axonivy-market/jira-connector/v10.0.0/jira-connector-product/images/create-issue.png "Create Jira issue")\n![jira-connector Demo 2](https://raw.githubusercontent.com/axonivy-market/jira-connector/v10.0.0/jira-connector-product/images/create-comment.png "Craete Jira comment")',
    isDependency: true,
    name: 'Jira Connector',
    groupId: 'com.axonivy.connector.jira',
    artifactId: 'jira-connector',
    type: 'iar'
  },
  _links: {
    self: {
      href: 'http://localhost:8082/api/product-details/jira-connector?type=connector'
    }
  }
};
