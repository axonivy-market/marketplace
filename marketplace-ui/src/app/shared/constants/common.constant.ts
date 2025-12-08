import { TypeOption } from '../enums/type-option.enum';
import { FeedbackSortType } from '../enums/feedback-sort-type';
import { Language } from '../enums/language.enum';
import { SortOption } from '../enums/sort-option.enum';
import { NavItem } from '../models/nav-item.model';
import { Pageable } from '../models/apis/pageable.model';
import { ItemDropdown } from '../models/item-dropdown.model';

export const MARKET_BASE_URL = 'https://market.axonivy.com/';

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
    label: 'common.footer.ivyCompanyInfo',
    link: 'common.footer.ivyCompanyInfoUrl'
  },
  {
    label: 'common.footer.privacyPolicy',
    link: 'common.footer.privacyPolicyUrl'
  },
  {
    label: 'common.footer.legalNotice',
    link: 'common.footer.legalNoticeUrl'
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

export const RATING_LABELS_BY_TYPE = [
  {
    type: 'connector',
    btnLabel: 'common.feedback.rateFeedbackForConnectorBtnLabel',
    noFeedbackLabel: 'common.feedback.noFeedbackForConnectorLabel'
  },
  {
    type: 'solution',
    btnLabel: 'common.feedback.rateFeedbackForSolutionBtnLabel',
    noFeedbackLabel: 'common.feedback.noFeedbackForSolutionLabel'
  },
  {
    type: 'util',
    btnLabel: 'common.feedback.rateFeedbackForUtilityBtnLabel',
    noFeedbackLabel: 'common.feedback.noFeedbackForUtilityLabel'
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
    value: 'description',
    label: 'common.product.detail.description'
  },
  {
    activeClass: "activeTab === 'demo'",
    value: 'demo',
    label: 'common.product.detail.demo'
  },
  {
    activeClass: "activeTab === 'setup'",
    value: 'setup',
    label: 'common.product.detail.installationGuide'
  },
  {
    activeClass: "activeTab === 'dependency'",
    value: 'dependency',
    label: 'common.product.detail.maven.label'
  },
  {
    activeClass: "activeTab === 'changelog'",
    tabId: 'tab-changelog',
    value: 'changelog',
    label: 'common.product.detail.changelog'
  }
];

export const FEEDBACK_APPROVAL_TABS: ItemDropdown[] = [
  {
    activeClass: "activeTab === 'review'",
    tabId: 'review-tab',
    value: 'review',
    label: 'Review feedback'
  },
  {
    activeClass: "activeTab === 'history'",
    tabId: 'history-tab',
    value: 'history',
    label: 'History'
  }
];

export const FEEDBACK_SORT_TYPES: ItemDropdown<FeedbackSortType>[] = [
  {
    value: FeedbackSortType.NEWEST,
    label: 'common.sort.value.newest'
  },
  {
    value: FeedbackSortType.OLDEST,
    label: 'common.sort.value.oldest'
  },
  {
    value: FeedbackSortType.HIGHEST,
    label: 'common.sort.value.highest'
  },
  {
    value: FeedbackSortType.LOWEST,
    label: 'common.sort.value.lowest'
  }
];

export const DESIGNER_SESSION_STORAGE_VARIABLE = {
  newIvyVersionParamName: 'ivy.version',
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

export const DEFAULT_MONITORING_PAGEABLE: Pageable = {
  page: 0,
  size: 10
};

export const DEFAULT_PAGEABLE_IN_REST_CLIENT: Pageable = {
  page: 0,
  size: 40
};

export const DEFAULT_CHANGELOG_PAGEABLE: Pageable = {
  page: 0,
  size: 5
};

export const VERSION = {
  displayPrefix: 'Version '
};

export const VERSION_PARAM = 'version';

export const I18N_ERROR_CODE_PATH = 'common.error.description';
export const I18N_DEFAULT_ERROR_CODE = 'default';

export const ERROR_PAGE = 'Error Page';
export const ERROR_PAGE_PATH = 'error-page';
export const NOT_FOUND_ERROR_CODE = 404;
export const INTERNAL_SERVER_ERROR_CODE = 500;
export const USER_NOT_FOUND_ERROR_CODE = 2103;
export const UNDEFINED_ERROR_CODE = 0;
export const BAD_REQUEST_ERROR_CODE = 400;
export const UNAUTHORIZED = 401;
export const FORBIDDEN = 403;
export const REQUEST_TIMEOUT = 408;
export const BAD_GATEWAY = 502;
export const SERVICE_UNAVAILABLE = 503;
export const GATEWAY_TIMEOUT = 504;
export const ERROR_CODES = [
  UNDEFINED_ERROR_CODE,
  NOT_FOUND_ERROR_CODE,
  INTERNAL_SERVER_ERROR_CODE,
  BAD_REQUEST_ERROR_CODE,
  UNAUTHORIZED,
  FORBIDDEN,
  REQUEST_TIMEOUT,
  BAD_GATEWAY,
  SERVICE_UNAVAILABLE,
  GATEWAY_TIMEOUT
];
export const TOKEN_KEY = 'token';

export const DEFAULT_IMAGE_URL = '/assets/images/misc/axonivy-logo-round.png';
export const DOWNLOAD_URL = 'https://developer.axonivy.com/download';
export const SEARCH_URL = 'https://developer.axonivy.com/search';
export const GITHUB_MARKET_ORG_URL = 'https://github.com/axonivy-market';
export const SHOW_DEV_VERSION = "showDevVersions";

export const SECONDS_IN_A_MINUTE = 60;
export const MINUTES_IN_A_HOUR = 60;
export const HOURS_IN_A_DAY = 24;
export const DAYS_IN_A_WEEK = 7;
export const DAYS_IN_A_MONTH = 30;
export const DAYS_IN_A_YEAR = 365;

export const MAX_FEEDBACK_LENGTH =250;

export const SECURITY_MONITOR_SESSION_KEYS = {
  DATA: 'security-monitor-data',
  TOKEN: 'security-monitor-token',
};

export const ERROR_MESSAGES = {
  TOKEN_REQUIRED: 'Token is required',
  UNAUTHORIZED_ACCESS: 'Unauthorized access.',
  FETCH_FAILURE: 'Failed to fetch security data. Check logs for details.',
  INVALID_TOKEN: 'The token is invalid, please try again.',
};

export const TIME_UNITS = [
  { SECONDS: 60, SINGULAR: 'minute', PLURAL: 'minutes' },
  { SECONDS: 3600, SINGULAR: 'hour', PLURAL: 'hours' },
  { SECONDS: 86400, SINGULAR: 'day', PLURAL: 'days' },
  { SECONDS: 604800, SINGULAR: 'week', PLURAL: 'weeks' },
  { SECONDS: 2592000, SINGULAR: 'month', PLURAL: 'months' },
  { SECONDS: 31536000, SINGULAR: 'year', PLURAL: 'years' },
];

export const REPO_PAGE_PATHS: Record<string, string> = {
  security: '/security',
  dependabot: '/security/dependabot',
  codeScanning: '/security/code-scanning',
  secretScanning: '/security/secret-scanning',
  branches: '/settings/branches',
  lastCommit: '/commit/',
};

export const HASH_SYMBOL = '#';
export const SRC = 'src';
export const APP = 'app';
export const DIST = 'dist';
export const BROWSER ='browser';
export const ASSETS = 'assets';
export const I18N = 'i18n';
export const JSON_EXTENSION = '.json';
export const UTF8 = 'utf8';

export const FEEDBACK_APPROVAL_STATE = 'feedback-approval';

export const FEEDBACK_APPROVAL_SESSION_TOKEN = 'feedback-approval-token';

export const GITHUB_PULL_REQUEST_NUMBER_REGEX = /pull\/(\d+)/;

export const UNESCAPE_GITHUB_CONTENT_REGEX = /\\([_*[\]()~`>#+=|{}.!-])/g;

// Open Graph Meta Tags
export const OG_TITLE_KEY = 'og:title';
export const OG_DESCRIPTION_KEY = 'og:description';
export const OG_IMAGE_KEY = 'og:image';
export const OG_IMAGE_TYPE_KEY = 'og:image:type';
export const OG_IMAGE_PNG_TYPE = 'image/png';

// Google constants
export const GOOGLE_PROGRAMMABLE_SEARCH_SCRIPT_ID = 'googleCSEScript';
export const GOOGLE_PRGORAMMABLE_SEARCH_SCRIPT_TYPE = 'text/javascript';
export const GOOGLE_PRGORAMMABLE_SEARCH_SCRIPT_SOURCE = 'https://cse.google.com/cse.js?cx=1434dfc0811d84f59';
export const GOOGLE = 'google';
export const GOOGLE_SEARCH = 'gcse-search';
export const GOOGLE_SEARCH_BAR_CLASS_NAME = '.gsc-control-cse';
export const GOOGLE_SEARCH_BAR_BACKGROUND_CLASS_NAME = 'bg-secondary';

// Theme Icon
export const LIGHT_ICON_CLASS = 'bi-moon';
export const DARK_ICON_CLASS = 'bi-sun';

// Local Storage Attribute
export const DATA_THEME = 'data-bs-theme';
export const DATA_THEME_ICON = 'data-theme-icon';
export const DATA_LANGUAGE = 'data-language';

// Monitoring constants
export const NAME_COLUMN = 'name';
export const CI_BUILD = 'CI';
export const DEV_BUILD = 'DEV';
export const E2E_BUILD = 'E2E';
export const MONITORING_WIKI_LINK = 'https://github.com/axonivy-market/market/wiki/c5-Monitoring';

export const ASCENDING = 'asc';
export const DESCENDING = 'desc';

export const FOCUSED_TAB = 'focused';
export const STANDARD_TAB = 'standard';

export const DEFAULT_MODE = 'default';
export const REPORT_MODE = 'report';
export const ALL_ITEMS_PAGE_SIZE = 9999;
