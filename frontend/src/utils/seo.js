/**
 * Centralized SEO & Meta Management Utility
 *
 * Updates document head metadata for public and private pages cleanly:
 * - Title, description, canonical URL
 * - Open Graph & Twitter cards
 * - Robots indexing instructions (index for public pages, noindex for private user areas)
 */

const BASE_URL = "https://samprepix.com";

function setMetaTag(attrName, attrValue, content) {
    if (typeof document === "undefined") return;

    let el = document.querySelector(`meta[${attrName}="${attrValue}"]`);
    if (!el) {
        el = document.createElement("meta");
        el.setAttribute(attrName, attrValue);
        document.head.appendChild(el);
    }
    el.setAttribute("content", content);
}

function setCanonical(url) {
    if (typeof document === "undefined") return;

    let el = document.querySelector('link[rel="canonical"]');
    if (!el) {
        el = document.createElement("link");
        el.setAttribute("rel", "canonical");
        document.head.appendChild(el);
    }
    el.setAttribute("href", url);
}

/**
 * Configure Page Metadata
 *
 * @param {Object} options
 * @param {string} options.title - Document title
 * @param {string} options.description - Meta description
 * @param {string} [options.canonicalPath] - Relative path for canonical link
 * @param {boolean} [options.noIndex=false] - When true, prevents search engines from indexing private pages
 */
export function updatePageSEO({
    title,
    description,
    canonicalPath = "",
    noIndex = false
}) {
    if (typeof document === "undefined") return;

    const fullTitle = title.includes("Samprepix")
        ? title
        : `${title} | Samprepix`;

    document.title = fullTitle;

    // Meta description
    if (description) {
        setMetaTag("name", "description", description);
        setMetaTag("property", "og:description", description);
        setMetaTag("name", "twitter:description", description);
    }

    // Canonical URL
    const canonicalUrl = `${BASE_URL}${canonicalPath.startsWith("/") ? canonicalPath : `/${canonicalPath}`}`;
    setCanonical(canonicalUrl);

    // Open Graph Title & URL
    setMetaTag("property", "og:title", fullTitle);
    setMetaTag("property", "og:url", canonicalUrl);
    setMetaTag("property", "og:type", "website");
    setMetaTag("property", "og:site_name", "Samprepix");

    // Twitter Card
    setMetaTag("name", "twitter:card", "summary_large_image");
    setMetaTag("name", "twitter:title", fullTitle);

    // Robots meta tag
    const robotsContent = noIndex ? "noindex, nofollow" : "index, follow, max-snippet:-1, max-image-preview:large";
    setMetaTag("name", "robots", robotsContent);
    setMetaTag("name", "googlebot", robotsContent);
}

export default {
    updatePageSEO
};
