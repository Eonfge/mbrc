package com.kelsos.mbrc.rest.requests;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.kelsos.mbrc.annotations.MetaType;
import com.kelsos.mbrc.annotations.QueueAction;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "type",
    "action",
    "id"
})

public class NowPlayingQueueRequest {

  @MetaType @JsonProperty("type") private String type;
  @QueueAction @JsonProperty("action") private String action;
  @JsonProperty("id") private int id;

  /**
   * @return The type
   */
  @MetaType
  @JsonProperty("type") public String getType() {
    return type;
  }

  /**
   * @param type The type
   */
  @JsonProperty("type") public NowPlayingQueueRequest setType(@MetaType String type) {
    this.type = type;
    return this;
  }

  /**
   * @return The action
   */
  @QueueAction
  @JsonProperty("action") public String getAction() {
    return action;
  }

  /**
   * @param action The action
   */
  @JsonProperty("action") public NowPlayingQueueRequest setAction(@QueueAction String action) {
    this.action = action;
    return this;
  }

  /**
   * @return The id
   */
  @JsonProperty("id") public int getId() {
    return id;
  }

  /**
   * @param id The id
   */
  @JsonProperty("id") public NowPlayingQueueRequest setId(int id) {
    this.id = id;
    return this;
  }

  @Override public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  @Override public int hashCode() {
    return new HashCodeBuilder().append(type)
        .append(action)
        .append(id)
        .toHashCode();
  }

  @Override public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if (!(other instanceof NowPlayingQueueRequest)) {
      return false;
    }
    NowPlayingQueueRequest rhs = ((NowPlayingQueueRequest) other);
    return new EqualsBuilder().append(type, rhs.type)
        .append(action, rhs.action)
        .append(id, rhs.id)
        .isEquals();
  }
}

