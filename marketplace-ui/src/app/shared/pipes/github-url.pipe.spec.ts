import { TestBed } from '@angular/core/testing';
import { GithubUrlPipe } from './github-url.pipe';

describe('GithubUrlPipe', () => {
  let pipe: GithubUrlPipe;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [GithubUrlPipe]
    });
    pipe = TestBed.inject(GithubUrlPipe);
  });

  it('should create an instance', () => {
    expect(pipe).toBeTruthy();
  });

  it('should transform github username to correct GitHub URL', () => {
    const username = 'john-doe';
    const expected = 'https://github.com/john-doe';
    expect(pipe.transform(username)).toBe(expected);
  });

  it('should handle usernames with numbers', () => {
    const username = 'user123';
    const expected = 'https://github.com/user123';
    expect(pipe.transform(username)).toBe(expected);
  });

  it('should handle single character usernames', () => {
    const username = 'a';
    const expected = 'https://github.com/a';
    expect(pipe.transform(username)).toBe(expected);
  });
});
