import { ElementRef, Renderer2 } from "@angular/core";

export class GoogleSearchBarUtils {
    static renderGoogleSearchBar(renderer: Renderer2): void {
        if (!document.getElementById('googleCSEScript')) {
            console.log("Main case");

            const script = renderer.createElement('script');
            script.id = 'googleCSEScript';
            script.type = 'text/javascript';
            script.async = true;
            script.src = 'https://cse.google.com/cse.js?cx=036cea36d5dbf4f2b';
            script.onload = () => {
                this.addCustomClassToSearchBar(renderer);
            };
            renderer.appendChild(document.body, script);
        }
        else {
            console.log("Else case");
            // If script is already loaded, manually trigger reinitialization
            if (window.hasOwnProperty('google') && (window as any).google.search) {
                (window as any).google.search.cse.element.render('gcse-search');
            }
        }
    }

    static addCustomClassToSearchBar(renderer: Renderer2): void {
        setTimeout(() => {
            const searchBoxList = document.querySelectorAll('.gsc-control-cse'); // Google's search bar container
            if (searchBoxList.length > 0) {
                // renderer.addClass(searchBox, 'bg-secondary');
                for(let i = 0; i < searchBoxList.length; i++) {
                    const searchBox = searchBoxList[i];
                    renderer.addClass(searchBox, 'bg-secondary');
                }
            }
        }, 1000); // Give Google time to load
    }
}