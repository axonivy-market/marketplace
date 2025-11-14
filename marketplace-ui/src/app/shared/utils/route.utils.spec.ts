import { PRODUCT_DETAIL_TABS } from '../constants/common.constant';
import { RouteUtils } from './route.utils';

describe('RouteUtils', () => {
  describe('getTabFragment', () => {
    it('should return the fragment if it matches a PRODUCT_DETAIL_TAB value', () => {
      const validTab = PRODUCT_DETAIL_TABS[0].value;
      expect(RouteUtils.getTabFragment(validTab)).toBe(validTab);
    });

    it('should return the first PRODUCT_DETAIL_TAB value if fragment is invalid', () => {
      expect(RouteUtils.getTabFragment('invalid')).toBe(
        PRODUCT_DETAIL_TABS[0].value
      );
    });

    it('should return the first PRODUCT_DETAIL_TAB value if fragment is null', () => {
      expect(RouteUtils.getTabFragment(null)).toBe(
        PRODUCT_DETAIL_TABS[0].value
      );
    });

    it('should return the first PRODUCT_DETAIL_TAB value if fragment is undefined', () => {
      // TypeScript will complain, but the method takes string | null, so use `any` to simulate
      expect(RouteUtils.getTabFragment(undefined as any)).toBe(
        PRODUCT_DETAIL_TABS[0].value
      );
    });
  });
});
