package com.samourai.whirlpool.cli.api.protocol.rest;

import com.samourai.wallet.send.beans.SweepPreview;
import java.util.Collection;
import java.util.stream.Collectors;

public class ApiSweepPreviewResponse {
  private Collection<ApiSweepPreview> sweepPreviews;

  public ApiSweepPreviewResponse(Collection<SweepPreview> sweepPreviews) {
    this.sweepPreviews =
        sweepPreviews.stream()
            .map(sweepPreview -> new ApiSweepPreview(sweepPreview))
            .collect(Collectors.toList());
  }

  public Collection<ApiSweepPreview> getSweepPreviews() {
    return sweepPreviews;
  }

  public void setSweepPreviews(Collection<ApiSweepPreview> sweepPreviews) {
    this.sweepPreviews = sweepPreviews;
  }
}
