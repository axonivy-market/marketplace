import { Injectable } from '@angular/core';

export interface ParsedLog {
  timestamp: string;
  level: string;
  message: string;
  prefix: string;
  messageContent: string;
  isLong: boolean;
  icon: string;
}

const LOG_HEADER_REGEX =
  /^(?<timestamp>\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}) (?<level>[A-Z]{1,10}) (?<firstMessage>[^\r\n]{0,1000})$/

const LOG_TIMESTAMP_PREFIX_REGEX = /^\d{4}-\d{2}-\d{2}\s/;
const LONG_MESSAGE_THRESHOLD = 150;

@Injectable({
  providedIn: 'root'
})
export class LogParserService {

  parseLog(logLine: string): ParsedLog {
    const [firstLine, ...remainingLines] = logLine.split('\n');
    const match = LOG_HEADER_REGEX.exec(firstLine);

    if (match?.groups) {
      const { timestamp, level, firstMessage } = match.groups;

      const messageLines = [firstMessage];
      for (const line of remainingLines) {
        if (LOG_TIMESTAMP_PREFIX_REGEX.test(line)) { break; }
        messageLines.push(line);
      }

      const message = messageLines.join('\n');
      const trimmedLevel = level.trim();
      const { prefix, content } = this.extractMessageParts(message);

      return {
        timestamp,
        message,
        prefix,
        level: trimmedLevel,
        messageContent: content,
        icon: this.getLogLevelIconClass(trimmedLevel),
        isLong: message.length > LONG_MESSAGE_THRESHOLD
      };
    }

    return {
      timestamp: new Date().toISOString(),
      level: 'INFO',
      message: logLine,
      prefix: '',
      messageContent: logLine,
      icon: this.getLogLevelIconClass('INFO'),
      isLong: logLine.length > LONG_MESSAGE_THRESHOLD
    };
  }

  private extractMessageParts(message: string) {
    const parts = message.split(' - ');
    if (parts.length > 1) {
      return {
        prefix: parts[0],
        content: parts.slice(1).join(' - ')
      };
    }
    return { prefix: '', content: message };
  }

  private getLogLevelIconClass(level: string): string {
    const icons: { [key: string]: string } = {
      DEBUG: 'bi-bug',
      INFO: 'bi-info-circle',
      WARN: 'bi-exclamation-circle',
      ERROR: 'bi-x-circle',
      FATAL: 'bi-stop-circle'
    };
    return icons[level] || 'bi-info-circle';
  }
}