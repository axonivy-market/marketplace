import { TestBed } from '@angular/core/testing';
import { AvatarUrlPipe } from './avatar-url.pipe';

describe('AvatarUrlPipe', () => {
  let pipe: AvatarUrlPipe;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AvatarUrlPipe]
    });
    pipe = TestBed.inject(AvatarUrlPipe);
  });

  it('should create an instance', () => {
    expect(pipe).toBeTruthy();
  });

  it('should transform imageId to correct avatar URL with string id', () => {
    const imageId = 'abc123';
    const expected = `/assets/team/avatar/${imageId}`;
    expect(pipe.transform(imageId)).toBe(expected);
  });

  it('should transform imageId to correct avatar URL with number id', () => {
    const imageId = 123;
    const expected = `/assets/team/avatar/${imageId}`;
    expect(pipe.transform(imageId)).toBe(expected);
  });
});
