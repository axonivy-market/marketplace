import { TypeOption } from '../enums/type-option.enum';
import { FeedbackSortType } from '../enums/feedback-sort-type';
import { Language } from '../enums/language.enum';
import { SortOption } from '../enums/sort-option.enum';
import { NavItem } from '../models/nav-item.model';
import { DetailTab } from '../../modules/product/product-detail/product-detail.component';

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
    styleClass: 'fab fa-linkedin',
    url: '/'
  },
  {
    styleClass: 'fab fa-xing',
    url: '/'
  },
  {
    styleClass: 'fab fa-youtube',
    url: '/'
  },
  {
    styleClass: 'fab fa-facebook',
    url: '/'
  }
];

export const IVY_FOOTER_LINKS = [
  {
    containerStyleClass: 'w-md-100 footer__ivy-tag',
    label: 'common.footer.ivyCompanyInfo'
  },
  {
    containerStyleClass: 'footer__ivy-policy-tag',
    label: 'common.footer.privacyPolicy'
  },
  {
    containerStyleClass: 'footer__ivy-term-of-service-tag',
    label: 'common.footer.termsOfService'
  }
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

export const FILTER_TYPES = [
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

export const SORT_TYPES = [
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

export const PRODUCT_DETAIL_TABS: DetailTab[] = [
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

export const FEEDBACK_SORT_TYPES = [
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