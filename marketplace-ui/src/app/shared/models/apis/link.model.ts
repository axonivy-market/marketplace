export interface Link {
    self: {
        href: string;
    };
    first?: {
        href: string;
    };
    next?: {
        href: string;
    };
    last?: {
        href: string;
    };
}