// Only this pipeline's output is safe to share across users. Older analyses may
// contain text supplied beside a URL, even if the public URL itself is identical.
export const PUBLIC_SOURCE_ANALYSIS_VERSION = 'public-post-v1';
