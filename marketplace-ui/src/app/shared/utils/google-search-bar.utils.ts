import { Renderer2 } from "@angular/core";
import { environment } from "../../../environments/environment";

export class GoogleSearchBarUtils {
    static renderGoogleSearchBar(renderer: Renderer2): void {
        if (!document.getElementById('googleCSEScript')) {
            const script = renderer.createElement('script');
            script.id = environment.googleProgrammableSearchScriptId;
            script.type = environment.googleProgrammableSearchScriptType;
            script.async = true;
            script.src = environment.googleProgrammableSearchScriptSource;
            script.onload = () => {
                this.addCustomClassToSearchBar(renderer);
            };
            renderer.appendChild(document.body, script);
        }
        // If script is already loaded, manually trigger reinitialization
        if (window.hasOwnProperty('google') && window.google.search) {
            window.google.search.cse.element.render('gcse-search');
        }
    }

    static addCustomClassToSearchBar(renderer: Renderer2): void {
        setTimeout(() => {
            const searchBoxList = document.querySelectorAll('.gsc-control-cse'); // Google's search bar container
            if (searchBoxList.length > 0) {
                for (let i = 0; i < searchBoxList.length; i++) {
                    const searchBox = searchBoxList[i];
                    renderer.addClass(searchBox, 'bg-secondary');
                }
            }
        }, 1000); // Give Google time to load
    }
}