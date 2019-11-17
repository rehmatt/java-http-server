import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Comment } from '../entity/comment';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CommentService {

  constructor(private http: HttpClient) { }

  private commentUrl = 'api/comments';

  getComments(): Observable<Comment[]> {
    return this.http.get<Comment[]>(this.commentUrl);
  }

  postComment(comment: Comment): Observable<Comment> {
    const body: string = JSON.stringify(comment);
    const httpOptions = {
      headers: new HttpHeaders({
          'Content-Type': 'application/json'
      })
    };
    return this.http.post<Comment>(this.commentUrl + '/comment', body, httpOptions);
  }

}
