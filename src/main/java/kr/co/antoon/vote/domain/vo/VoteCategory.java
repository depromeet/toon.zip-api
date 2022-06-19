package kr.co.antoon.vote.domain.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VoteCategory {
    AB("ab"),
    MULTIPLE("choice");

    private final String description;
}
