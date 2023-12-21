package pl.com.tt.flex.user.refreshView;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RememberUserFilterDetails {

  private OfferFilterDTO offerFilterDTO;
  private String login;
  private Long fspId;
}
