package com.sila.config;

import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupOpenApiDTO implements Serializable {
  private String groupTitle;
  @Builder.Default private List<String> pathToMatches = new ArrayList<>();
}
