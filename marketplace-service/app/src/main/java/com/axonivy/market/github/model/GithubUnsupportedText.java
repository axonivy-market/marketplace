package com.axonivy.market.github.model;

public record GithubUnsupportedText(
    String deprecatedMessage,
    String removeUnsupportedNoticeMessage,
    String unsupportedBranchName,
    String removeUnsupportedNoticePrBody,
    String addUnsupportedNoticePrBody,
    String unsupportedNotice
) {
}

