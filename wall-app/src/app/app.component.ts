import { Component, OnInit } from '@angular/core';
import { CommentService } from './service/comment.service';
import { Comment } from './entity/comment';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'Wall Application';
  comments: Comment[];
  text: string;

  constructor(private commentService: CommentService) {

  }

  ngOnInit() {
    this.getComments();
  }

  getComments() {
    this.commentService.getComments().subscribe(data => {
        this.comments = data;
    });
  }

  postComment() {
  this.commentService.postComment(new Comment(undefined, this.text)).subscribe(data => {
      const tmp = [...this.comments];
      tmp.push(data);
      this.text = undefined;
      this.comments = tmp;
  });
  }
}
