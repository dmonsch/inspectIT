class EventSelector {

    public readonly events: string[];
    public readonly markAlwaysAsRelevant: boolean;

    private readonly selector: string;
    private readonly attributesToExtract: string[];

    public constructor(config: [string, string, string, boolean]) {
        this.events = config[0].split(",");
        this.selector = config[1];
        this.attributesToExtract = config[2].split(",");
        this.markAlwaysAsRelevant = config[3];
    }

    public matchesElement(elem: Element) {
        return Util.elementMatchesSelector(elem, this.selector);
    }

    public extractAttributes(elem: Element, storage: IDictionary<string>) {
        for (const attributeName of this.attributesToExtract) {
            if (!(attributeName in storage)) {
                if (attributeName === "$label") {
                    const label = this.getLabelText(elem);
                    if (label) {
                        storage[attributeName] = label;
                    }
                } else {
                    if (elem.hasAttribute(attributeName)) {
                        let htmlAttr = elem.getAttribute(attributeName);
                        if (htmlAttr == null) {
                            htmlAttr = "";
                        }
                        storage[attributeName] = htmlAttr.toString();
                    } else if ((elem as any)[attributeName] !== undefined && (elem as any)[attributeName] !== "") {
                        storage[attributeName] = (elem as any)[attributeName].toString();
                    }
                }
            }
        }
    }

    private getLabelText(elem: any) {
        if ((typeof elem.parentElement) === "object" && (typeof elem.parentElement.getElementsByTagName) === "function") {
            const parent = (elem as Node).parentElement;
            if (parent !== null) {
                const labels = parent.getElementsByTagName("LABEL");
                for (let i = 0; i < labels.length; i++) {
                    const label = labels.item(i);
                    if (label.getAttribute("for") === elem.id) {
                        return (label as any).innerText;
                    }
                }
            }
        }
        return null;
    }

}