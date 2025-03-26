import { Renderer2 } from "@angular/core";
import { GoogleSearchBarUtils } from "./google-search-bar.utils";

describe("GoogleSearchBarUtils", () => {
    let renderer: jasmine.SpyObj<Renderer2>;

    beforeEach(() => {
        renderer = jasmine.createSpyObj("Renderer2", ["createElement", "appendChild", "addClass"]);
    });

    describe("renderGoogleSearchBar", () => {
        let getElementByIdSpy: jasmine.Spy;

        beforeEach(() => {
            getElementByIdSpy = spyOn(document, "getElementById");
        });
    
        it("should create and append script if not already present", () => {
            getElementByIdSpy.and.returnValue(null); // Simulating that script is not present
            const scriptMock = {} as HTMLScriptElement;
            renderer.createElement.and.returnValue(scriptMock);
    
            GoogleSearchBarUtils.renderGoogleSearchBar(renderer);
    
            expect(renderer.createElement).toHaveBeenCalledWith("script");
            expect(renderer.appendChild).toHaveBeenCalledWith(document.body, scriptMock);
        });
    
        it("should not append script if already present", () => {
            getElementByIdSpy.and.returnValue(document.createElement("script")); // Simulating script already present

            // Ensure `window.google` exists before spying on it
            (window as any).google = { search: { cse: { element: { render: jasmine.createSpy("render") } } } };
        
            GoogleSearchBarUtils.renderGoogleSearchBar(renderer);
        
            expect(renderer.createElement).not.toHaveBeenCalled();
            expect(renderer.appendChild).not.toHaveBeenCalled();
            expect(window.google.search.cse.element.render).toHaveBeenCalledWith("gcse-search");
        
            // Clean up: Remove `window.google` to avoid conflicts in other tests
            delete (window as any).google;
        });
    });

    describe("addCustomClassToSearchBar", () => {
        it("should add class to search box elements", (done) => {
            const searchBox = document.createElement("div");
            searchBox.classList.add("gsc-control-cse");
            document.body.appendChild(searchBox);
            
            GoogleSearchBarUtils.addCustomClassToSearchBar(renderer);

            setTimeout(() => {
                expect(renderer.addClass).toHaveBeenCalledWith(searchBox, "bg-secondary");
                document.body.removeChild(searchBox);
                done();
            }, 1100); // Exceeding 1000ms delay to ensure execution
        });
    });
});
