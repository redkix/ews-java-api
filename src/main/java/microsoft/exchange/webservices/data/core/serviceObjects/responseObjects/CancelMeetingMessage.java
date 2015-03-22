/*
 * The MIT License
 * Copyright (c) 2012 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package microsoft.exchange.webservices.data.core.serviceObjects.responseObjects;

import microsoft.exchange.webservices.data.core.serviceObjects.items.Item;
import microsoft.exchange.webservices.data.core.serviceObjects.items.MeetingCancellation;
import microsoft.exchange.webservices.data.attributes.ServiceObjectDefinition;
import microsoft.exchange.webservices.data.core.XmlElementNames;
import microsoft.exchange.webservices.data.core.serviceObjects.schemas.CancelMeetingMessageSchema;
import microsoft.exchange.webservices.data.core.serviceObjects.schemas.ServiceObjectSchema;
import microsoft.exchange.webservices.data.enumerations.ExchangeVersion;
import microsoft.exchange.webservices.data.exceptions.ServiceLocalException;
import microsoft.exchange.webservices.data.properties.complex.MessageBody;

/**
 * Represents a meeting cancellation message.
 */
@ServiceObjectDefinition(xmlElementName = XmlElementNames.CancelCalendarItem, returnedByServer = false)
public final class CancelMeetingMessage extends
    CalendarResponseMessageBase<MeetingCancellation> {

  /**
   * Initializes a new instance of the class.
   *
   * @param referenceItem the reference item
   * @throws Exception the exception
   */
  public CancelMeetingMessage(Item referenceItem) throws Exception {
    super(referenceItem);
  }

  /**
   * Gets the minimum required server version.
   *
   * @return Earliest Exchange version in which this service object type is
   * supported.
   */
  @Override public ExchangeVersion getMinimumRequiredServerVersion() {
    return ExchangeVersion.Exchange2007_SP1;
  }

  /**
   * Gets the minimum required server version.
   *
   * @return Earliest Exchange version in which this service object type is
   * supported.
   */
  @Override public ServiceObjectSchema getSchema() {
    return CancelMeetingMessageSchema.Instance;
  }

  /**
   * Gets the body of the response.
   *
   * @return the body
   * @throws ServiceLocalException the service local exception
   */
  public MessageBody getBody() throws ServiceLocalException {
    return (MessageBody) this.getPropertyBag()
        .getObjectFromPropertyDefinition(
            CancelMeetingMessageSchema.Body);
  }

  /**
   * Sets the body.
   *
   * @param value the new body
   * @throws Exception the exception
   */
  public void setBody(MessageBody value) throws Exception {
    this.getPropertyBag().setObjectFromPropertyDefinition(
        CancelMeetingMessageSchema.Body, value);
  }

}
