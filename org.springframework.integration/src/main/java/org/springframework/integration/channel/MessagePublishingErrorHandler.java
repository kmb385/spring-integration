/*
 * Copyright 2002-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.channel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.integration.core.MessageChannel;
import org.springframework.integration.message.ErrorMessage;
import org.springframework.integration.message.MessagingException;
import org.springframework.integration.util.ErrorHandler;

/**
 * {@link ErrorHandler} implementation that sends an {@link ErrorMessage} to a
 * {@link MessageChannel}.
 * 
 * @author Mark Fisher
 */
public class MessagePublishingErrorHandler implements ErrorHandler {

	private final Log logger = LogFactory.getLog(this.getClass());

	private volatile MessageChannel errorChannel;

	private final long sendTimeout = 1000;


	public MessagePublishingErrorHandler() {
	}

	public MessagePublishingErrorHandler(MessageChannel errorChannel) {
		this.errorChannel = errorChannel;
	}


	public void setErrorChannel(MessageChannel errorChannel) {
		this.errorChannel = errorChannel;
	}

	public final void handle(Throwable t) {
		if (logger.isWarnEnabled()) {
			if (t instanceof MessagingException) {
				logger.warn("failure occurred in messaging task with message: "
						+ ((MessagingException) t).getFailedMessage(), t);
			}
			else {
				logger.warn("failure occurred in messaging task", t);
			}
		}
		if (this.errorChannel != null) {
			try {
				if (this.errorChannel instanceof BlockingChannel && this.sendTimeout >= 0) {
					((BlockingChannel) this.errorChannel).send(new ErrorMessage(t), this.sendTimeout);
				}
				else {
					this.errorChannel.send(new ErrorMessage(t));
				}
			}
			catch (Throwable ignore) { // message will be logged only
			}
		}
	}

}
