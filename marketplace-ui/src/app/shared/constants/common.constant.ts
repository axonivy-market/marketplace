import { TypeOption } from '../enums/type-option.enum';
import { FeedbackSortType } from '../enums/feedback-sort-type';
import { Language } from '../enums/language.enum';
import { SortOption } from '../enums/sort-option.enum';
import { NavItem } from '../models/nav-item.model';
import { DetailTab } from '../../modules/product/product-detail/product-detail.component';
import { Pageable } from '../models/apis/pageable.model';
import { ItemDropdown } from '../models/item-dropdown.model';

export const NAV_ITEMS: NavItem[] = [
  {
    label: 'common.nav.news',
    link: 'https://developer.axonivy.com/news'
  },
  {
    label: 'common.nav.doc',
    link: 'https://developer.axonivy.com/doc'
  },
  {
    label: 'common.nav.tutorial',
    link: 'https://developer.axonivy.com/tutorial'
  },
  {
    label: 'common.nav.community',
    link: 'https://community.axonivy.com/'
  },
  {
    label: 'common.nav.team',
    link: 'https://developer.axonivy.com/team'
  },
  {
    label: 'common.nav.market',
    link: '/'
  }
];

export const SOCIAL_MEDIA_LINK = [
  {
    styleClass: 'fa-brands fa-linkedin-in',
    title: 'Axon Ivy | LinkedIn',
    url: 'https://www.linkedin.com/company/axon-ivy-ag'
  },
  {
    styleClass: 'fa-brands fa-xing',
    title: 'Axon Ivy | XING',
    url: 'https://www.xing.com/companies/axonivyag'
  },
  {
    styleClass: 'fa-brands fa-facebook-f',
    title: 'Axon Ivy | Facebook',
    url: 'https://www.facebook.com/axonivy'
  },
  {
    styleClass: 'fa-brands fa-youtube',
    title: 'Axon Ivy | Youtube',
    url: 'https://www.youtube.com/channel/UCkoNcDoeDAVM7FB-txy3jnQ'
  },
];

export const IVY_FOOTER_LINKS = [
  {
    containerStyleClass: 'w-md-100 footer__ivy-tag',
    label: 'common.footer.ivyCompanyInfo',
    link: 'https://www.axonivy.com/'
  },
  {
    containerStyleClass: 'footer__ivy-policy-tag',
    label: 'common.footer.privacyPolicy',
    link: 'https://www.axonivy.com/privacy-policy'
  },
];

export const LANGUAGES = [
  {
    value: Language.DE,
    label: 'DE'
  },
  {
    value: Language.EN,
    label: 'EN'
  }
];

export const FILTER_TYPES: ItemDropdown<TypeOption>[] = [
  {
    value: TypeOption.All_TYPES,
    label: 'common.filter.value.allTypes'
  },
  {
    value: TypeOption.CONNECTORS,
    label: 'common.filter.value.connector'
  },
  {
    value: TypeOption.UTILITIES,
    label: 'common.filter.value.util'
  },
  {
    value: TypeOption.SOLUTION,
    label: 'common.filter.value.solution'
  }
];

export const SORT_TYPES: ItemDropdown<SortOption>[] = [
  {
    value: SortOption.STANDARD,
    label: 'common.sort.value.standard'
  },
  {
    value: SortOption.POPULARITY,
    label: 'common.sort.value.popularity'
  },
  {
    value: SortOption.ALPHABETICALLY,
    label: 'common.sort.value.alphabetically'
  },
  {
    value: SortOption.RECENT,
    label: 'common.sort.value.recent'
  }
];

export const PRODUCT_DETAIL_TABS: ItemDropdown[] = [
  {
    activeClass: "activeTab === 'description'",
    tabId: 'description-tab',
    value: 'description',
    label: 'common.product.detail.description'
  },
  {
    activeClass: "activeTab === 'demo'",
    tabId: 'demo-tab',
    value: 'demo',
    label: 'common.product.detail.demo'
  },
  {
    activeClass: "activeTab === 'setup'",
    tabId: 'setup-tab',
    value: 'setup',
    label: 'common.product.detail.installationGuide'
  },
  {
    activeClass: "activeTab === 'dependency'",
    tabId: 'dependency-tab',
    value: 'dependency',
    label: 'common.product.detail.maven.label'
  }
];

export const FEEDBACK_SORT_TYPES: ItemDropdown<FeedbackSortType>[] = [
  {
    value: FeedbackSortType.NEWEST,
    label: 'common.sort.value.newest',
    sortFn: 'updatedAt,desc'
  },
  {
    value: FeedbackSortType.OLDEST,
    label: 'common.sort.value.oldest',
    sortFn: 'updatedAt,asc'
  },
  {
    value: FeedbackSortType.HIGHEST,
    label: 'common.sort.value.highest',
    sortFn: 'rating,desc'
  },
  {
    value: FeedbackSortType.LOWEST,
    label: 'common.sort.value.lowest',
    sortFn: 'rating,asc'
  }
];

export const DESIGNER_COOKIE_VARIABLE = {
  ivyViewerParamName: 'ivy-viewer',
  ivyVersionParamName: 'ivy-version',
  defaultDesignerViewer: 'designer-market',
  restClientParamName: 'resultsOnly',
  searchParamName: 'search'
};

export const DEFAULT_PAGEABLE: Pageable = {
  page: 0,
  size: 20
};
export const DEFAULT_PAGEABLE_IN_REST_CLIENT: Pageable = {
  page: 0,
  size: 40
};

export const VERSION = {
  tagPrefix: 'v',
  displayPrefix: 'Version '
};

export const ERROR_PAGE_PATH = '/error-page';
export const NOT_FOUND_ERROR_CODE = 404;
export const INTERNAL_SERVER_ERROR_CODE = 500;
export const UNDEFINED_ERROR_CODE = 0;
export const ERROR_CODES = [
  UNDEFINED_ERROR_CODE,
  NOT_FOUND_ERROR_CODE,
  INTERNAL_SERVER_ERROR_CODE
];

export const DEFAULT_IMAGE_URL = '/assets/images/misc/axonivy-logo-round.png';
