import { ObjectId } from './objectid';

export class Comment {
  private _id: ObjectId;
  private text: string;

  constructor(_id: ObjectId, text: string) {
     this._id = _id;
     this.text = text;
   }
}
