export class CommonUtils {

  static getLabel(value: string, options: any): string {
    const currentLabel = options.find((option: { value: string, label: string; }) => option.value === value)?.label;
    return currentLabel || options[0].label;
  }

}
