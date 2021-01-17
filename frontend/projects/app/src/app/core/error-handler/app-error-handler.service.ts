import { ErrorHandler, Injectable } from "@angular/core";

import { environment } from "../../../environments/environment";

import { NotificationService } from "../notifications/notification.service";
import { ServerError } from "../server-error/server-error.model";

/** Application-wide error handler that adds a UI notification to the error handling
 * provided by the default Angular ErrorHandler.
 */
@Injectable()
export class AppErrorHandler extends ErrorHandler {
  constructor(private notificationsService: NotificationService) {
    super();
  }

  handleError(error: Error | ServerError) {
    let displayMessage;
    if (error instanceof ServerError) {
      switch (error.status) {
        case 400:
          displayMessage = "Validation errors occurred! ";
          if (error.errors && error.errors.length > 0) {
            displayMessage += "Please correct above errors.";
          } else {
            displayMessage += error.message;
          }
          break;
        case 409:
          displayMessage = "Conflict: " + error.message;
          break;
        default:
          displayMessage = error.message;
      }
    } else {
      displayMessage = "An error occurred.";
      if (!environment.production) {
        displayMessage += " See console for details.";
      }
    }
    this.notificationsService.error(displayMessage);
    super.handleError(error);
  }
}
