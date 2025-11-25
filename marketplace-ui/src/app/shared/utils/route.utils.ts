import { PRODUCT_DETAIL_TABS } from "../constants/common.constant";

export class RouteUtils {
  static getTabFragment(fragment: string | null): string {
    const isValidTab = PRODUCT_DETAIL_TABS.some(
      tab => tab.value === fragment
    );
    if (isValidTab && fragment) {
      return fragment;
    }
    return PRODUCT_DETAIL_TABS[0].value;
  }
}
